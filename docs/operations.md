# Operations Runbook

Runbook for the production deployment of `jbytow.pl`. It replaces the deployment steps in `context.md` and `README.md`.

## Overview

Production is a Proxmox VM (`<server>`) running Ubuntu Server and Docker Compose with `docker-compose.prod.yml`. The checkout lives in `<repo-dir>`.

| Service | Container | Image | Data | Ports |
|---|---|---|---|---|
| `postgres` | `devportfolio-db-prod` | `postgres:16-alpine` | volume `<project>_postgres_data_prod` | internal only |
| `backend` | `devportfolio-backend-prod` | built from `./backend` (Spring Boot) | volume `<project>_uploads_data_prod` at `/app/uploads` | internal 8080 |
| `frontend` | `devportfolio-frontend-prod` | built from `./frontend` (Vite build served by nginx) | none | internal 80 |
| `nginx` | `devportfolio-nginx` | `nginx:1.30-alpine` (pinned) | bind mount `nginx/nginx.conf`, read-only | published `127.0.0.1:80` |

- Cloudflare Tunnel terminates TLS at Cloudflare. `cloudflared` runs as a systemd service on the same VM and forwards the public hostname to `localhost:80`.
- The edge nginx publishes port 80 on `127.0.0.1` only. There is no 443 listener and no certificate on the VM, so a LAN client cannot bypass Cloudflare or forge `CF-Connecting-IP`.
- The edge is pinned to nginx 1.30 because `resolve` in `nginx/nginx.conf` needs upstream support available from 1.27.3.
- The frontend container serves the SPA and proxies `/oauth2/` and `/login/oauth2/` to the backend. In production `/api/` is proxied by the edge directly to the backend.
- All four services use json-file logging (`max-size: 10m`, `max-file: 3`), `restart: unless-stopped`, and the bridge network `devportfolio-network-prod`. `security_opt: no-new-privileges` is set on backend, frontend and nginx, intentionally not on postgres: its entrypoint starts as root and drops to the postgres user itself.
- Compose prefixes volume names with the project name (the checkout directory name, lowercased), so the uploads volume is `<project>_uploads_data_prod`. Check the real name once with `docker volume ls --filter name=uploads_data_prod` and use it in the backup and restore commands below.
- Deploy as the user that owns the checkout, never as root. Files created by root in `.git` block later pulls and deploys.
- The development compose (`docker-compose.yml`) publishes 5432, 8080 and 5173 on `127.0.0.1` only.

## Environment

Compose reads `.env` from the repository root. It is never committed; `.env.example` documents the keys.

Required. Compose refuses to start without them (`${VAR:?}` in the compose file):

| Variable | Consumed by | Notes |
|---|---|---|
| `POSTGRES_DB` | postgres, backend | database name, also used by the healthcheck, `pg_dump` and restore |
| `POSTGRES_USER` | postgres, backend | also used by `pg_dump` and restore |
| `POSTGRES_PASSWORD` | postgres, backend | |
| `JWT_SECRET` | backend | at least 32 bytes and different from the development default |
| `GOOGLE_CLIENT_ID` | backend | OAuth client |
| `GOOGLE_CLIENT_SECRET` | backend | OAuth client |
| `ADMIN_EMAILS` | backend | comma separated allowlist of admin addresses |
| `FRONTEND_URL` | backend | public origin, for example `https://jbytow.pl` |

Optional:

| Variable | Default | Notes |
|---|---|---|
| `MAIL_HOST` | `smtp.gmail.com` | |
| `MAIL_PORT` | `587` | |
| `MAIL_USERNAME` | empty | contact mail will not work when blank |
| `MAIL_PASSWORD` | empty | Gmail app password |
| `CONTACT_EMAIL` | empty | recipient of the contact form |
| `VITE_API_URL` | `/api` | build argument of the frontend image |

Startup validations and their effects:

| Validation | Behaviour |
|---|---|
| `JWT_SECRET` missing (development default) or shorter than 32 bytes, `prod` profile | backend refuses to start |
| `JWT_SECRET` rotated | every admin session is invalidated |
| `ADMIN_EMAILS` changed | re-checked on every request, so removing an address takes effect from the next request |
| Google e-mail not verified (`email_verified`) | login is rejected |
| `FRONTEND_URL` wrong | CORS and OAuth redirects use the wrong origin; admin writes from another origin get 403 under the Origin check |
| Flyway `baseline-on-migrate` off | a database without `flyway_schema_history` does not start; it needs a manual baseline |

Check the schema history before a deploy:

```bash
docker compose -f docker-compose.prod.yml exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT version, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"
```

The empty result means the database has no migration history and the backend will not start without a baseline.

## Deploying the backend

Run everything from the repository root in `<repo-dir>`.

1. Update the checkout.

```bash
git pull
```

2. Check whether the pull brought a migration.

```bash
ls backend/src/main/resources/db/migration/V*.sql | tail -n 5
```

The current history is `V1..V8`. `V7` drops redundant indexes and sets `posts.published` NOT NULL (NULL rows become `false`); `V8` deletes `users` rows outside the admin allowlist (`is_admin IS NOT TRUE`). Both change or remove data, so a dump before deploying them is mandatory. Any new migration gets the same treatment.

3. Dump the database and the uploads volume before a deploy with a new migration. The dump directory lives outside the repository and has to be created once.

```bash
mkdir -p /opt/backups
set -a; . ./.env; set +a
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > /opt/backups/devportfolio-$(date +%F-%H%M).sql.gz
docker run --rm -v "$(docker volume ls -q --filter name=uploads_data_prod)":/data -v /opt/backups:/backup alpine tar czf /backup/uploads-$(date +%F).tgz -C /data .
```

4. Rebuild and restart the backend.

```bash
docker compose -f docker-compose.prod.yml up -d --build backend
```

5. Watch the startup and the first requests.

```bash
docker compose -f docker-compose.prod.yml logs --since 5m backend
```

- No edge restart is needed after a backend deploy: the `resolver 127.0.0.11` and `resolve` in the upstreams re-read the container address.
- `/api/health` mirrors the database state and answers 503 when the database is unreachable.
- The health response does not include the mail check, so an SMTP outage does not mark the container unhealthy (the image HEALTHCHECK calls `/api/health`).

## Deploying the frontend

The frontend is built as a Docker image; the compiled SPA and the container nginx config are baked into it. The fork has no `deploy.sh`, so a deploy is a rebuild and a recreate:

```bash
git pull
docker compose -f docker-compose.prod.yml up -d --build frontend
```

- `VITE_API_URL` is a build argument read from `.env`. Changing it requires this rebuild.
- The container nginx serves the SPA with `try_files ... /index.html` for client-side routes, caches hashed assets for one year, and proxies `/oauth2/` and `/login/oauth2/` to the backend.
- Recreating the container briefly interrupts the frontend; the edge re-resolves `frontend` through Docker DNS afterwards.

## Deploying edge nginx and compose changes

- `nginx/nginx.conf` is a single-file bind mount. An edit inside the running container would be lost and nginx does not reload the file on its own, so after a pull that changes it:

```bash
docker compose -f docker-compose.prod.yml up -d --force-recreate nginx
docker compose -f docker-compose.prod.yml exec nginx nginx -t
```

- Compose-level changes (log rotation limits, `security_opt`, published ports) apply only when containers are recreated. The first `up -d` after such a change recreates the affected services, which means a short downtime.
- Rate limiting is per visitor: the key is `CF-Connecting-IP` with a fallback to the socket address. Throttled requests get 429. Without Cloudflare in front, every visitor shares the connector address and therefore one bucket.

| Zone | Rate | Applied to |
|---|---|---|
| `api` | 10 r/s, burst 20 | `/api/` |
| `media` | 30 r/s, burst 100 | `/api/media/` |
| `oauth` | 10 r/min, burst 10 | `/oauth2/`, `/login/oauth2/` |
| `contact` | 5 r/min, burst 3 | exact `/api/contact` |
| `general` | 30 r/s, burst 50 | everything else |

- `Content-Security-Policy-Report-Only` is sent from the edge. Violations appear in the browser console only; there is no report endpoint. Switch to the enforcing `Content-Security-Policy` header after a week without violations.
- Forwarded headers are set at the edge: single-value `X-Forwarded-For` with the visitor IP, plus `X-Forwarded-Host`, `X-Forwarded-Port` 443 and `X-Forwarded-Proto` https. Client-supplied values are overwritten or cleared because Spring trusts them, including for the OAuth `redirect_uri`.
- Diagnostics from another host must go through the public hostname. On the VM itself, request the edge on loopback.

## Verification after deploy

On the VM, against the edge on loopback:

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1/api/health                 # 200
curl -s -o /dev/null -w '%{http_code} %{redirect_url}\n' http://127.0.0.1/api/admin/auth/me  # 401, no Location
curl -s http://127.0.0.1/api/.env                                                    # {"error":"not found"}
curl -sI http://127.0.0.1/ | grep -i content-security-policy-report-only             # header present
```

From outside, through the public hostname:

```bash
curl -sI https://jbytow.pl/ | head -n 1                        # 200
curl -s https://jbytow.pl/api/posts | head -c 100              # JSON array of published posts
```

Then in a browser: log in as admin (verified Google e-mail, address in `ADMIN_EMAILS`, request Origin matching `FRONTEND_URL`) and publish a post from the panel.

Container and log checks:

```bash
docker compose -f docker-compose.prod.yml ps                                   # postgres, backend, frontend healthy; nginx Up
docker compose -f docker-compose.prod.yml logs --since 10m backend | grep -E 'WARN|ERROR'
docker compose -f docker-compose.prod.yml logs --since 10m nginx | tail
```

## Periodic tasks

- Back up the database and the uploads volume nightly. The names come from `.env`; escape `%` as `\%` in a crontab entry. Keep the last 14 days.

```bash
mkdir -p /opt/backups
set -a; . ./.env; set +a
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > /opt/backups/devportfolio-$(date +%F-%H%M).sql.gz
docker run --rm -v "$(docker volume ls -q --filter name=uploads_data_prod)":/data -v /opt/backups:/backup alpine tar czf /backup/uploads-$(date +%F).tgz -C /data .
find /opt/backups -type f -mtime +14 -delete
```

- Remove build leftovers after a deploy run with new images.

```bash
docker system prune -f
```

- Review Dependabot pull requests weekly (frontend npm, backend Maven, base image tags).
- Refresh base images when upstream publishes security fixes and rebuild.

```bash
docker compose -f docker-compose.prod.yml build --pull
```

- After a week without CSP-Report-Only violations, replace the Report-Only header with the enforcing `Content-Security-Policy` header in `nginx/nginx.conf` and recreate the edge.
- Update `cloudflared` from Cloudflare's apt repository. It runs outside unattended-upgrades, so upgrade it by hand and restart the service.

```bash
sudo apt-get update
sudo apt-get install --only-upgrade cloudflared
sudo systemctl restart cloudflared
```

## Database role without superuser (planned)

The backend and Flyway connect as the role created by the postgres image (`POSTGRES_USER`), which is a superuser. Move the application to a least-privilege role at the next database task. Procedure, one-off on the server during a maintenance window and after a dump:

```bash
set -a; . ./.env; set +a
docker compose -f docker-compose.prod.yml exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<'SQL'
CREATE ROLE devportfolio_app LOGIN PASSWORD 'CHANGE_ME';
GRANT CONNECT ON DATABASE devportfolio TO devportfolio_app; -- replace devportfolio with the value of POSTGRES_DB
GRANT USAGE, CREATE ON SCHEMA public TO devportfolio_app;
DO $$ DECLARE r record; BEGIN
  FOR r IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
    EXECUTE format('ALTER TABLE public.%I OWNER TO devportfolio_app', r.tablename);
  END LOOP;
  FOR r IN SELECT sequencename FROM pg_sequences WHERE schemaname = 'public' LOOP
    EXECUTE format('ALTER SEQUENCE public.%I OWNER TO devportfolio_app', r.sequencename);
  END LOOP;
END $$;
SQL
```

Flyway must own the tables it alters, hence the ownership transfer (including `flyway_schema_history`). Then point the backend at the new role: add `APP_DB_USER=devportfolio_app` and `APP_DB_PASSWORD=...` to `.env`, change `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` of the `backend` service in `docker-compose.prod.yml` to those variables (the `postgres` service keeps `POSTGRES_USER`), run `docker compose -f docker-compose.prod.yml up -d backend`, and check the startup log for Flyway validation. Rollback: switch the two variables back and restart the backend. Not applied yet: the compose file still uses `POSTGRES_USER` for the backend.

## When something breaks

- Backend container restarting or `unhealthy`: read the log and look for a missing `JWT_SECRET` or one shorter than 32 bytes, a database without `flyway_schema_history`, or a failed migration. Run Flyway `repair` only after analysing what drifted; it does not fix a bad script.
- 502 from the edge after recreating backend or frontend: force a recreate so the resolver re-reads the upstreams after a network change.

```bash
docker compose -f docker-compose.prod.yml up -d --force-recreate nginx
```

- 429 for ordinary visitors: the visitor falls into a rate-limit zone. Check the zone rates and the burst in `nginx/nginx.conf`. If Cloudflare is not in front of the VM, `CF-Connecting-IP` is absent and all visitors share the connector address.
- Admin cannot log in: check that the address is in `ADMIN_EMAILS`, that the Google account has a verified e-mail, and that the browser Origin matches `FRONTEND_URL`. Admin writes from another origin get 403.
- Mail not sent: `/api/contact` answers 503 and logs the SMTP exception class. Check `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` (Gmail app password) and `CONTACT_EMAIL`. The health check does not include mail, so the container stays healthy.
- Edge serves a stale configuration after a pull: the container was not recreated, run the `--force-recreate nginx` command above and confirm with `nginx -t`.
- Frontend shows a blank page after a deploy: check that the container is running and that its log has no startup error, then reload; the image serves the current `index.html` and the hashed assets from that build.

## Restore from backup

Restore only against a stopped backend, otherwise clients write into a partially restored schema.

1. Stop the backend and start postgres.

```bash
docker compose -f docker-compose.prod.yml stop backend
docker compose -f docker-compose.prod.yml up -d postgres
```

2. Either restore into a fresh volume, or clear the current schema and replay the dump.

```bash
set -a; . ./.env; set +a
docker compose -f docker-compose.prod.yml exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
gunzip -c /opt/backups/devportfolio-2026-11-11-0300.sql.gz | docker compose -f docker-compose.prod.yml exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"
```

3. Start the backend. Flyway sees the restored `flyway_schema_history` and continues from its recorded version.

```bash
docker compose -f docker-compose.prod.yml up -d backend
```

4. Restore the uploads volume if the dump is older than the files on disk.

```bash
docker run --rm -v "$(docker volume ls -q --filter name=uploads_data_prod)":/data -v /opt/backups:/backup alpine sh -c 'cd /data && tar xzf /backup/uploads-2026-11-11.tgz'
```

5. Run the verification block above.

The uploads restore command is the reverse of the backup command and has never been exercised in production: to be confirmed.

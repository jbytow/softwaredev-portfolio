# API reference

REST API of the backend. All endpoints are JSON over HTTPS, except media downloads.

## Conventions

**Base path.** Every endpoint lives under `/api`. The frontend talks to the API through the site origin; a separate gateway is not required.

**Authentication.** Admin endpoints under `/api/admin/**` require a logged-in administrator. Login uses Google OAuth2 at `/oauth2/authorization/google`; on success the backend sets an HttpOnly cookie `auth_token` containing a JWT (a `Authorization: Bearer <token>` header is accepted too). On every request the application checks that the token is valid, that the e-mail is verified (`email_verified`) and that the e-mail is still present in the `ADMIN_EMAILS` configuration, so removing an address revokes access before the token expires. Anonymous calls to `/api/**` return 401 without a redirect to the login page. A valid cookie whose account has no admin role returns 403. State-changing requests (anything but GET, HEAD, OPTIONS) to `/api/admin/**`, including the logout, are also rejected with 403 when they carry an `Origin` (or `Referer`) that does not match `FRONTEND_URL`; requests without either header pass, so non-browser clients keep working. Accounts that are not on the allowlist or have an unverified e-mail are rejected at login. `POST /api/admin/auth/logout` clears the cookie and requires no authentication. Login failures redirect to `/admin/login?error=not_authorized` (allowlist or unverified e-mail) or `/admin/login?error=oauth_failed` (provider error).

**Localization.** Public post responses contain only the fields of the requested language. The language comes from the `Accept-Language` header: `pl` selects Polish, anything else falls back to English. The unused `*En` or `*Pl` fields are then `null`, and `hasCaseStudy` is computed from the requested language's fields only. Admin post responses keep both languages (`*En` and `*Pl`) next to the resolved fields the edit form needs.

**Response envelope.** Every JSON response uses the `ApiResponse` envelope. Fields that are null are omitted from the output (`@JsonInclude(NON_NULL)`), so successful responses carry only `success` and `data` — no `message`.

Success:

```json
{
  "success": true,
  "data": [
    { "slug": "example-post" }
  ]
}
```

Error:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": "must not be blank"
  }
}
```

`errors` is present only for validation failures and maps a field name to its message.

**Error codes.**

| Code | Meaning |
|---|---|
| 400 | Invalid request: validation failed, malformed JSON, unknown enum value, missing part or parameter, invalid reorder payload, duplicate slug on update |
| 401 | Missing or invalid admin session; no body |
| 403 | Authenticated account without the admin role, or an admin write with a foreign `Origin`/`Referer` |
| 404 | Entity, slug or media file not found; an unknown path answers `{"error":"not found"}` |
| 405 | Method not supported for the path |
| 406 | `Accept` header cannot accept a JSON answer; returned without a body |
| 409 | Conflict with an existing record, for example a unique constraint hit by two concurrent saves, or a field length limit |
| 413 | Uploaded file exceeds the configured size limit |
| 415 | Unsupported content type |
| 429 | Rate limit exceeded at the edge |
| 500 | Unexpected server error |
| 503 | Health check reports `DOWN`, or mail delivery failed when submitting the contact form |

**Rate limits.** Limits are enforced at the edge per visitor, keyed on `CF-Connecting-IP`: 10 requests per second for `/api/`, 30 requests per second for `/api/media/`, 10 requests per minute for `/oauth2/` and `/login/oauth2/`, 5 requests per minute for `/api/contact`, 30 requests per second for everything else. Exceeding a limit returns 429.

## Public endpoints

| Method | Path | Purpose | Notes |
|---|---|---|---|
| POST | `/api/contact` | Submit the contact form | `name` (max 100), `email`, `subject` (max 200), `message` (max 5000); optional `website` is a honeypot (when filled the request answers 200 and nothing is sent); mail failure returns 503 |
| GET | `/api/achievements` | List achievements | Ordered by display order |
| GET | `/api/experiences` | List experiences | Ordered by display order |
| GET | `/api/health` | Health check | Mirrors the aggregated Actuator health (database included): 200 `{"status":"UP",...}` or 503 `{"status":"DOWN",...}`; adds `service` and `timestamp`, no component details; used by the external monitor because `/actuator/health` is not exposed by the proxy |
| GET | `/api/interests` | List interests | Ordered by display order |
| GET | `/api/media/{subDir}/{filename}` | Serve an uploaded file | Names are random UUIDs; responds with `Cache-Control: public, max-age=31536000, immutable`; a missing file or unknown subdirectory returns 404 |
| GET | `/api/posts` | List published posts | Optional `category` and `hashtag` query parameters; `hashtag` takes precedence; one language per request |
| GET | `/api/posts/{slug}` | Get a published post by slug | One language per request; an unknown or unpublished slug returns the same 404 |
| GET | `/api/rpg-stats` | List RPG stats | Ordered by display order |
| GET | `/api/settings` | Get public site settings | |
| GET | `/api/skill-categories/with-skills` | List skill categories together with their soft skills | Single call for the skills section |

## Admin endpoints

All endpoints in this section require the admin session described in Conventions.

### Auth

| Method | Path | Purpose | Notes |
|---|---|---|---|
| POST | `/api/admin/auth/logout` | Log out | Clears the auth cookie; no authentication required, but a foreign `Origin` is rejected with 403 |
| GET | `/api/admin/auth/me` | Get the current admin | Returns the account identified by the cookie; 401 without one |

### Posts

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/posts` | List all posts | Optional `category` filter; returns both languages and drafts |
| POST | `/api/admin/posts` | Create a post | Request body validated with `@Valid`; returns 201; slug generated from `titleEn` when empty, with collisions suffixed |
| PATCH | `/api/admin/posts/reorder` | Reorder posts | Body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| DELETE | `/api/admin/posts/{id}` | Delete a post | Also deletes the post's media rows and stored files; returns a success envelope with no data |
| GET | `/api/admin/posts/{id}` | Get a post by id | Returns both languages; also feeds the admin draft preview |
| PUT | `/api/admin/posts/{id}` | Update a post | Request body validated with `@Valid`; an empty slug keeps the previous one; duplicate slug returns 400 (409 only when two concurrent saves race on the database constraint) |
| PATCH | `/api/admin/posts/{id}/publish` | Toggle the published flag | Returns the updated post |

### Media

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/media` | List all media | Optional `type` filter; pageable (`page`, `size` default 20, `sort`) |
| GET | `/api/admin/media/unassigned` | List media not attached to a post | Pageable, `size` default 20 |
| POST | `/api/admin/media` | Upload a file | Multipart: `file`, optional `postId`, `altTextEn`, `altTextPl`; types come from `app.upload.allowed-types` (images, MP4/WebM video, PDF); 50 MB limit, oversize returns 413 |
| PATCH | `/api/admin/media/reorder` | Reorder media | Query parameter `postId`; body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| POST | `/api/admin/media/youtube` | Register a YouTube video | Query parameters `videoUrl`, optional `postId`, `altTextEn`, `altTextPl`; accepts watch, shorts, live, embed, youtu.be and nocookie links; returns 201 |
| DELETE | `/api/admin/media/{id}` | Delete a media record | Also removes the stored file |
| PUT | `/api/admin/media/{id}` | Update a media record | Query parameters `altTextEn`, `altTextPl`, `postId` |

### Experiences

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/experiences` | List all experiences | |
| POST | `/api/admin/experiences` | Create an experience | Returned with 201 |
| PATCH | `/api/admin/experiences/reorder` | Reorder experiences | Body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| DELETE | `/api/admin/experiences/{id}` | Delete an experience | |
| GET | `/api/admin/experiences/{id}` | Get an experience by id | |
| PUT | `/api/admin/experiences/{id}` | Update an experience | |

### Interests

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/interests` | List all interests | |
| POST | `/api/admin/interests` | Create an interest | Returned with 201 |
| PATCH | `/api/admin/interests/reorder` | Reorder interests | Body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| DELETE | `/api/admin/interests/{id}` | Delete an interest | |
| GET | `/api/admin/interests/{id}` | Get an interest by id | |
| PUT | `/api/admin/interests/{id}` | Update an interest | |

### Achievements

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/achievements` | List all achievements | |
| POST | `/api/admin/achievements` | Create an achievement | Returned with 201 |
| PATCH | `/api/admin/achievements/reorder` | Reorder achievements | Body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| DELETE | `/api/admin/achievements/{id}` | Delete an achievement | |
| GET | `/api/admin/achievements/{id}` | Get an achievement by id | |
| PUT | `/api/admin/achievements/{id}` | Update an achievement | |

### RPG stats

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/rpg-stats` | List all RPG stats | |
| POST | `/api/admin/rpg-stats` | Create an RPG stat | Returned with 201 |
| PATCH | `/api/admin/rpg-stats/reorder` | Reorder RPG stats | Body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| DELETE | `/api/admin/rpg-stats/{id}` | Delete an RPG stat | |
| GET | `/api/admin/rpg-stats/{id}` | Get an RPG stat by id | |
| PUT | `/api/admin/rpg-stats/{id}` | Update an RPG stat | |

### Skill categories

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/skill-categories` | List all skill categories | |
| POST | `/api/admin/skill-categories` | Create a skill category | Returned with 201 |
| PATCH | `/api/admin/skill-categories/reorder` | Reorder skill categories | Body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| DELETE | `/api/admin/skill-categories/{id}` | Delete a skill category | |
| GET | `/api/admin/skill-categories/{id}` | Get a skill category by id | |
| PUT | `/api/admin/skill-categories/{id}` | Update a skill category | |

### Soft skills

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/soft-skills` | List all soft skills | |
| POST | `/api/admin/soft-skills` | Create a soft skill | Returned with 201 |
| PATCH | `/api/admin/soft-skills/reorder` | Reorder soft skills | Body `{"items":[{"id":"<uuid>","displayOrder":<int>}]}`; empty or null elements return 400 |
| DELETE | `/api/admin/soft-skills/{id}` | Delete a soft skill | |
| GET | `/api/admin/soft-skills/{id}` | Get a soft skill by id | |
| PUT | `/api/admin/soft-skills/{id}` | Update a soft skill | |

### Settings

| Method | Path | Purpose | Notes |
|---|---|---|---|
| GET | `/api/admin/settings` | Get site settings | |
| PUT | `/api/admin/settings` | Update site settings | |

## Changes from previous versions

Endpoints and behaviours that existed earlier and are no longer available:

- `GET /api/soft-skills` and `GET /api/soft-skills/{id}` removed; soft skills are read through `GET /api/skill-categories/with-skills`.
- `GET /api/skill-categories` and `GET /api/skill-categories/{id}` removed; `GET /api/skill-categories/with-skills` remains.
- `GET /api/experiences/{id}`, `GET /api/interests/{id}`, `GET /api/achievements/{id}` and `GET /api/rpg-stats/{id}` removed; the list endpoints are the public read path.
- `GET /api/media/{id}` removed; files are read at `/api/media/{subDir}/{filename}`.
- `GET /api/posts/paged` and `GET /api/posts/hashtags` removed; hashtag filtering stays as a query parameter on `GET /api/posts`.
- `GET /api/categories` and `GET /api/categories/{category}` removed (the frontend uses its local enum and i18n labels).
- Successful responses no longer carry a `message` field.
- Create endpoints answer 201 instead of 200.
- Public slug lookups no longer return drafts (404 like an unknown slug); drafts are reachable only through the admin endpoints.
- Anonymous calls to `/api/**` answer 401 instead of redirecting to Google.
- An invalid request body answers 400 with per-field messages in `errors`.
- A slug already in use answers 400 on update (409 only when two concurrent saves race on the database constraint).
- Public post responses contain only the requested language's fields; the other language's `*En`/`*Pl` fields are `null` and `hasCaseStudy` is computed per language.

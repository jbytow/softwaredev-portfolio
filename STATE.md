# STATE - softwareDevPortfolioApp
Aktualizacja: 2026-11-12 (sesja audytu, zapis awaryjny przy 85% limitu Claude)

## Cel bieżący
Przeniesienie 56 punktów audytu 2026-11 z bliźniaka marketingPortfolioApp (przewodnik:
D:/Dev/Projects/Java/marketingPortfolioApp/docs/audit-2026-11-porting-guide.md) na gałąź `audit-2026-11`
(z `main` eca0ec8). Etap 1: 50 dotyczy, 6 częściowo, 0 nie dotyczy. Etap 2: 17 paczek w worktree `.worktrees/<pkg>`
(gałęzie `audit/<pkg>`), każda z recenzją Opus. Scalone do `audit-2026-11`: fale 1-3 + spring-boot-upgrade (HEAD 7519492),
docs/api.md i docs/operations.md (28ab59b). Artefakty sesji: `%TEMP%\sdpa-audit` (ledger.md, briefs/, reports/, reviews/, etap1.json).

## Ostatnio zrobione
- Fala 1: backend-security, backend-errors-contact-config, backend-media, build-deps-images, edge-and-compose, frontend-tooling.
- Fala 2: backend-db-persistence (V7, V8, MigrationSchemaTest), backend-api-contract, public-pages, app-shell, media-and-richtext, admin-errors-and-cleanup.
- Fala 3: admin-session-cache-preview, edge-csp-and-frontend-cleanup, ci-and-dependabot. Fala 4: spring-boot-upgrade (Boot 4.1.2).
- Weryfikacja po fali 3: frontend typecheck/lint(0 warn)/319 testów/build OK; backend po Boot 4.1.2: 499 testów, 0 skipped (log final-backend.log).
- README.md przepisany na stan obecny (niezacommitowany).

## W toku
- Paczka admin-i18n (pkt 40): worktree `.worktrees/admin-i18n`, 2 commity (122763b, 4aded50), weryfikacja zielona; recenzja Opus w toku
  (agent działa w tle). Po recenzji: poprawki uwag, merge do `audit-2026-11`.
- Etap 3 pozostało: merge admin-i18n, pełne testy po merge, smoke compose (świeża baza, V1-V8, 401/404), audyt regresji świeżym agentem,
  LESSONS.md (lekcje + usunięte duble), commit README/STATE, raport końcowy, sprzątanie worktree.

## Otwarte decyzje właściciela (do raportu końcowego)
- Zmiany widoczne: 401 zamiast 302, usunięte publiczne trasy (/api/categories, /{id}, paged, hashtags, soft-skills), szkice 404 po slugu,
  V8 usuwa konta nie-adminów (pg_dump przed wdrożeniem), edge tylko 127.0.0.1:80, ConfirmDeleteModal, modal wygasłej sesji, podgląd szkicu,
  strona 404, lokalne czcionki, fasada YouTube, publiczne wpisy tylko z polami żądanego języka, Boot 4.1 (400 "Invalid request" przy nadmiarowych tokenach JSON).
- Host/ścieżka produkcji forka nieznane (homelab: VM 301 "MyPortfolio" status plan) -> placeholdery `<server>`/`<repo-dir>` w docs/operations.md.
- Wpisy do "Challenges During Development" w README: tylko za zgodą (propozycje w raporcie).
- Placeholdery w AdminSettings ("Natalia Zakulecka", "Zakulecka") to pozostałość po oryginale: poza zakresem audytu, do decyzji.
- Merge `audit-2026-11` -> `main` i wdrożenie: tylko na "wdrażaj".

## Następne kroki
1. Wynik recenzji admin-i18n -> poprawki -> `git merge --no-ff audit/admin-i18n` do audit-2026-11.
2. Pełne testy (backend z DOCKER_HOST, frontend), smoke `docker compose up -d --build` w WSL, audyt regresji (świeży agent Opus).
3. LESSONS.md, commit README/STATE/LESSONS, raport końcowy z tabelą 56 punktów (etap1-table.tsv) i blokiem "Do zrobienia".
4. Sprzątanie: `git worktree remove .worktrees/<pkg>` x17 (najpierw usunąć frontend/node_modules w worktree frontendowych; bez --force przy junction), gałęzie audit/*, forwarder socat (`wsl docker rm -f sdpa-tc-socat`).

## Znane problemy / pułapki
- Lokalny `java` = JDK 25; zawsze `JAVA_HOME="/c/Program Files/Java/latest/jdk-21"`. Testy DB: forwarder socat w WSL na 127.0.0.1:2375
  (`DOCKER_HOST=tcp://127.0.0.1:2375 TESTCONTAINERS_RYUK_DISABLED=true`).
- DeepSeek: brief przez ścieżkę w `-p` kończył się exit 9; działa przez stdin.
- Vitest uruchamiać z katalogu frontend (alias `@/` nie działa z roota).

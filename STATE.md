# STATE - softwareDevPortfolioApp
Aktualizacja: 2026-11-12 (audyt scalony do main i wypchnięty; wdrożenie na prod nie wykonane)

## Cel bieżący
Przeniesienie 56 punktów audytu 2026-11 z bliźniaka marketingPortfolioApp (przewodnik:
D:/Dev/Projects/Java/marketingPortfolioApp/docs/audit-2026-11-porting-guide.md) na gałąź `audit-2026-11` (z `main` eca0ec8).
Etap 1: 50 dotyczy, 6 częściowo, 0 nie dotyczy. Etap 2: 17 paczek (worktree `.worktrees/<pkg>`, gałęzie `audit/<pkg>`),
każda z niezależną recenzją diffu i poprawkami. WSZYSTKIE 17 paczek scalone do `audit-2026-11`: HEAD 7e51ca6 (ostatni merge: admin-i18n).
Artefakty sesji audytu (dziennik paczek, briefy, raporty, recenzje) leżą poza repo, w katalogu tymczasowym stacji roboczej.

## Ostatnio zrobione
- Fale 1-4 scalone (backend-security, errors-config, media, build-deps, edge, frontend-tooling; db-persistence V7/V8 + MigrationSchemaTest,
  api-contract, public-pages, app-shell, media-and-richtext, admin-errors-and-cleanup; admin-session-cache-preview, edge-csp, ci; spring-boot-upgrade 4.1.2; admin-i18n).
- Weryfikacja na HEAD: backend 531 testów, 0 błędów, 0 pominiętych (JDK 21, Testcontainers przez DOCKER_HOST); frontend typecheck OK,
  lint 0 ostrzeżeń, vitest 51 plików / 319 testów, build OK (entry 117 kB). LOC: backend main 5528->5805, test 2591->8079; frontend src 9565->10367 (+4129 testów).
- Docs: docs/api.md, docs/operations.md (szkic z kodu + korekty), README przepisany na stan obecny (7699c9c), LESSONS.md L11-L14 (sync do D:\Dev\_lekcje).
- 77 commitów + 17 merge; `git log eca0ec8..HEAD` bez atrybucji AI.

## W toku
- Nic. `main` = 5748bc5 (fast-forward z `audit-2026-11`, wypchnięte na origin). Paczka porządkowa po audycie regresji scalona (e037f7e, 5748bc5): martwe `Post.hasCaseStudy()` i klucze query usunięte, logi odrzuconych logowań bez e-maila, `isAdmin(null)` bezpieczne, `LocaleUtilsTest`, placeholdery neutralne, Challenges 7-9 w README. Testy: backend 0 błędów / 0 pominiętych (Testcontainers), frontend 319, lint 0, build OK.

## Następne kroki
1. CI na GitHubie po pushu `main` (pierwszy przebieg `.github/workflows/ci.yml`): sprawdzić wynik, zwłaszcza job backend z Testcontainers.
2. Wdrożenie na prod TYLKO na "wdrażaj": .env (JWT_SECRET >= 32 B, FRONTEND_URL, 8 wymaganych zmiennych), pg_dump + kopia uploadów (V7/V8), `up -d --build`, `up -d --force-recreate nginx`, weryfikacja z docs/operations.md, próbne logowanie admina.
3. Uzupełnić `<server>`/`<repo-dir>` w docs/operations.md, gdy znany jest host produkcyjny forka.
4. Lokalne sprzątanie: stack `sdpa-smoke` (`docker compose -p sdpa-smoke -f docker-compose.yml down -v` w WSL), forwarder `docker rm -f sdpa-tc-socat`, katalogi tymczasowe audytu; gałąź `audit-2026-11` można usunąć (`git branch -d audit-2026-11`).

## Otwarte decyzje właściciela (do raportu końcowego)
- Zmiany widoczne: 401 zamiast 302, usunięte publiczne trasy (/api/categories, /{id} dla experiences/interests/achievements/rpg-stats, paged, hashtags, soft-skills),
  szkice 404 po slugu + podgląd w panelu, V8 usuwa konta nie-adminów, edge tylko 127.0.0.1:80 bez 443, ConfirmDeleteModal, modal wygasłej sesji, useUnsavedChanges,
  strona 404, lokalne czcionki, fasada YouTube, publiczne wpisy tylko z polami żądanego języka, Boot 4.1 (400 "Invalid request" przy nadmiarowych tokenach JSON), i18n panelu.
- Host/ścieżka produkcji forka nieznane (homelab: VM 301 "MyPortfolio" status plan) -> placeholdery `<server>`/`<repo-dir>` w docs/operations.md.
- Wpisy "Challenges During Development" w README: tylko za zgodą (propozycje w raporcie: port audytu między bliźniakami; cicho pomijane testy DB; motyw przed Reactem a CSP).
- Poza zakresem: placeholdery "Natalia Zakulecka"/"Zakulecka" w AdminSettings (pozostałość oryginału); `Post.hasCaseStudy()` martwe; PageImpl WARN w AdminMediaController;
  45 podatności npm wymagających major (vite 8, vitest 5, tiptap 3, router 7); rola bazy bez superusera; CSP Report-Only -> enforcing po tygodniu.

## Znane problemy / pułapki
- Lokalny `java` = JDK 25; zawsze `JAVA_HOME="/c/Program Files/Java/latest/jdk-21"`. Testy DB: forwarder socat w WSL na 127.0.0.1:2375
  (`DOCKER_HOST=tcp://127.0.0.1:2375 TESTCONTAINERS_RYUK_DISABLED=true`); Flyway + validate w profilu test, bez Dockera testy DB są pomijane.
- Wywołania WSL z Git Bash: `MSYS_NO_PATHCONV=1` przy ścieżkach /mnt/....
- Vitest uruchamiać z katalogu frontend (alias `@/` nie działa z roota).

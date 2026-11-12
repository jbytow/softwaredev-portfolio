# STATE - softwareDevPortfolioApp
Aktualizacja: 2026-11-12 (sesja audytu; checkpoint przy 93% limitu Claude)

## Cel bieżący
Przeniesienie 56 punktów audytu 2026-11 z bliźniaka marketingPortfolioApp (przewodnik:
D:/Dev/Projects/Java/marketingPortfolioApp/docs/audit-2026-11-porting-guide.md) na gałąź `audit-2026-11` (z `main` eca0ec8).
Etap 1: 50 dotyczy, 6 częściowo, 0 nie dotyczy. Etap 2: 17 paczek (worktree `.worktrees/<pkg>`, gałęzie `audit/<pkg>`),
każda z recenzją Opus i poprawkami. WSZYSTKIE 17 paczek scalone do `audit-2026-11`: HEAD 7e51ca6 (ostatni merge: admin-i18n).
Artefakty sesji: `%TEMP%\sdpa-audit` (ledger.md, briefs/, reports/, reviews/, etap1.json, table.md, smoke.sh, logi).

## Ostatnio zrobione
- Fale 1-4 scalone (backend-security, errors-config, media, build-deps, edge, frontend-tooling; db-persistence V7/V8 + MigrationSchemaTest,
  api-contract, public-pages, app-shell, media-and-richtext, admin-errors-and-cleanup; admin-session-cache-preview, edge-csp, ci; spring-boot-upgrade 4.1.2; admin-i18n).
- Weryfikacja na HEAD: backend 531 testów, 0 błędów, 0 pominiętych (JDK 21, Testcontainers przez DOCKER_HOST); frontend typecheck OK,
  lint 0 ostrzeżeń, vitest 51 plików / 319 testów, build OK (entry 117 kB). LOC: backend main 5528->5805, test 2591->8079; frontend src 9565->10367 (+4129 testów).
- Docs: docs/api.md, docs/operations.md (DeepSeek + korekty), README przepisany na stan obecny (7699c9c), LESSONS.md L11-L14 (sync do D:\Dev\_lekcje).
- 77 commitów + 17 merge; `git log eca0ec8..HEAD` bez atrybucji AI.

## W toku (zadania w tle, wyniki w %TEMP%\sdpa-audit)
- Smoke compose w WSL (`smoke.sh`, projekt `sdpa-smoke`, dev compose, świeża baza): wynik w `smoke.log`. Kontenery po teście tylko `stop`
  (klasyfikator odrzucił `down -v`): do ręcznego `docker compose -p sdpa-smoke -f docker-compose.yml down -v` w WSL.
- Audyt regresji (świeży agent Opus): raport do `regression-audit.md` (tabela 56 punktów + regresje + higiena). Po nim: ewentualne poprawki, raport końcowy.

## Następne kroki
1. Odczytać smoke.log i regression-audit.md; poprawki regresji nowym commitem na audit-2026-11 (TDD), ponowne testy celowane.
2. Raport końcowy po polsku: tabela 56 punktów (table.md + werdykty audytu), testy/LOC przed i po (baseline.md), decyzje właściciela, "Do zrobienia".
3. Sprzątanie (po akceptacji): `git worktree remove --force .worktrees/<pkg>` x17 (node_modules są prawdziwymi katalogami, nie junction), `git branch -D audit/<pkg>`,
   forwarder `wsl docker rm -f sdpa-tc-socat`, `%TEMP%\sdpa-audit`, `%TEMP%\sdpa-review-scratch` (scratch recenzenta), stack `sdpa-smoke`.
4. Merge `audit-2026-11` -> `main` i wdrożenie TYLKO na "wdrażaj" (przed: .env prod JWT_SECRET >= 32 B, pg_dump, `up -d --force-recreate nginx`, próbne logowanie).

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
- DeepSeek: brief przez ścieżkę w `-p` dał exit 9; działa przez stdin. Wywołania WSL z Git Bash: `MSYS_NO_PATHCONV=1` przy ścieżkach /mnt/....
- Vitest uruchamiać z katalogu frontend (alias `@/` nie działa z roota).

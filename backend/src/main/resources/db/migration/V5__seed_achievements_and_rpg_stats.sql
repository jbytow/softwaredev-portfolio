-- Seed achievements (from former completedQuests)
INSERT INTO achievements (title_en, title_pl, description_en, description_pl, icon, year, display_order) VALUES
('The Automation Crusade', 'Krucjata Automatyzacji', 'Saved 180+ hours/month through process automation at Unilever', 'Zaoszczędzono 180+ godzin/miesiąc dzięki automatyzacji procesów w Unilever', 'zap', '2021', 0),
('Coffeetica Deployed', 'Coffeetica Wdrożona', 'Built and deployed a full-stack coffee review platform', 'Zbudowano i wdrożono pełnostackową platformę recenzji kawy', 'rocket', '2024', 1),
('The Scholar''s Path', 'Ścieżka Uczonego', 'Became a university lecturer teaching IT and data analytics', 'Został wykładowcą akademickim prowadzącym zajęcia z IT i analizy danych', 'award', '2023', 2),
('The Centralization Quest', 'Quest Centralizacji', 'Led financial data centralization across Unilever brands', 'Poprowadzono centralizację danych finansowych w markach Unilever', 'database', '2020', 3),
('Career Class Change', 'Zmiana Klasy Postaci', 'Transitioned from finance professional to software engineer', 'Przekwalifikowanie z finansisty na inżyniera oprogramowania', 'sparkles', '2024', 4);

-- Seed RPG stats (from former rpgStats)
INSERT INTO rpg_stats (attr, label_en, label_pl, level, max_level, skills, display_order) VALUES
('STR', 'Java / Spring', 'Java / Spring', 7, 10, 'Spring Boot, Hibernate, REST API, JUnit', 0),
('INT', 'Python / Data', 'Python / Dane', 6, 10, 'Django, Pandas, NumPy, Beautiful Soup', 1),
('WIS', 'Finance & Analysis', 'Finanse i Analiza', 9, 10, 'VBA, Power Query, SQL, Power BI', 2),
('DEX', 'React / Frontend', 'React / Frontend', 6, 10, 'React, TypeScript, Tailwind, Framer Motion', 3),
('CON', 'DevOps / Infra', 'DevOps / Infra', 5, 10, 'Docker, NGINX, Ubuntu Server, Cloudflare', 4),
('CHA', 'Teaching / Comm', 'Nauczanie / Komunikacja', 7, 10, 'Lecturing, Team Leadership, English C1', 5);

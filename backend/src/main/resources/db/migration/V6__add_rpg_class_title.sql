ALTER TABLE site_settings ADD COLUMN rpg_class_title_en TEXT DEFAULT 'Level 12 Finance Wizard turned Code Knight';
ALTER TABLE site_settings ADD COLUMN rpg_class_title_pl TEXT DEFAULT 'Poziom 12 Czarodziej Finansów przekwalifikowany na Rycerza Kodu';

UPDATE site_settings SET rpg_class_title_en = 'Level 12 Finance Wizard turned Code Knight', rpg_class_title_pl = 'Poziom 12 Czarodziej Finansów przekwalifikowany na Rycerza Kodu' WHERE id = 1;

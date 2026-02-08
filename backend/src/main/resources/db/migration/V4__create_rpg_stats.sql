CREATE TABLE rpg_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attr VARCHAR(10) NOT NULL,
    label_en VARCHAR(255) NOT NULL,
    label_pl VARCHAR(255) NOT NULL,
    level INTEGER DEFAULT 5,
    max_level INTEGER DEFAULT 10,
    skills TEXT,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

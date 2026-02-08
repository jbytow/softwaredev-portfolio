-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Posts table
CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category VARCHAR(50) NOT NULL,
    title_en VARCHAR(255) NOT NULL,
    title_pl VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    excerpt_en TEXT,
    excerpt_pl TEXT,
    content_en JSONB,
    content_pl JSONB,
    featured_image VARCHAR(500),
    github_url VARCHAR(500),
    live_url VARCHAR(500),
    published BOOLEAN DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    hashtags TEXT[] DEFAULT '{}',
    case_study_challenge_en TEXT,
    case_study_challenge_pl TEXT,
    case_study_solution_en TEXT,
    case_study_solution_pl TEXT,
    case_study_results_en TEXT,
    case_study_results_pl TEXT,
    case_study_testimonial_en TEXT,
    case_study_testimonial_pl TEXT,
    case_study_testimonial_author VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_posts_category ON posts(category);
CREATE INDEX idx_posts_published ON posts(published);
CREATE INDEX idx_posts_slug ON posts(slug);
CREATE INDEX idx_posts_display_order ON posts(display_order);
CREATE INDEX idx_posts_hashtags ON posts USING GIN (hashtags);

-- Media table
CREATE TABLE media (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID REFERENCES posts(id) ON DELETE SET NULL,
    type VARCHAR(20) NOT NULL,
    filename VARCHAR(255),
    original_name VARCHAR(255),
    mime_type VARCHAR(100),
    size BIGINT,
    url VARCHAR(500) NOT NULL,
    alt_text_en VARCHAR(255),
    alt_text_pl VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    video_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_post_id ON media(post_id);
CREATE INDEX idx_media_type ON media(type);

ALTER TABLE media ADD CONSTRAINT media_type_check
    CHECK (type IN ('IMAGE', 'VIDEO', 'PDF', 'YOUTUBE'));

-- Experiences table
CREATE TABLE experiences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title_en VARCHAR(255) NOT NULL,
    title_pl VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    description_en TEXT,
    description_pl TEXT,
    achievements_en JSONB,
    achievements_pl JSONB,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_experiences_display_order ON experiences(display_order);
CREATE INDEX idx_experiences_start_date ON experiences(start_date);

-- Skill categories table
CREATE TABLE skill_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name_en VARCHAR(255) NOT NULL,
    name_pl VARCHAR(255) NOT NULL,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed default skill categories
INSERT INTO skill_categories (name_en, name_pl, display_order) VALUES
    ('Backend', 'Backend', 1),
    ('Frontend', 'Frontend', 2),
    ('Databases', 'Bazy Danych', 3),
    ('DevOps & Tools', 'DevOps i Narzędzia', 4),
    ('Soft Skills', 'Umiejętności Miękkie', 5);

-- Soft skills table
CREATE TABLE soft_skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id UUID REFERENCES skill_categories(id) ON DELETE SET NULL,
    name_en VARCHAR(255) NOT NULL,
    name_pl VARCHAR(255) NOT NULL,
    description_en TEXT,
    description_pl TEXT,
    professional_usage_en TEXT,
    professional_usage_pl TEXT,
    icon VARCHAR(100),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_soft_skills_display_order ON soft_skills(display_order);

-- Interests table
CREATE TABLE interests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_en VARCHAR(255) NOT NULL,
    title_pl VARCHAR(255) NOT NULL,
    image1 VARCHAR(500),
    image2 VARCHAR(500),
    image3 VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Site settings table (singleton)
CREATE TABLE site_settings (
    id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    hero_title_en VARCHAR(255),
    hero_title_pl VARCHAR(255),
    hero_subtitle_en TEXT,
    hero_subtitle_pl TEXT,
    about_text_en TEXT,
    about_text_pl TEXT,
    profile_image VARCHAR(500),
    email VARCHAR(255),
    phone VARCHAR(50),
    social_links JSONB,
    meta_description_en TEXT,
    meta_description_pl TEXT,
    footer_title_en VARCHAR(255),
    footer_title_pl VARCHAR(255),
    footer_tagline_en TEXT,
    footer_tagline_pl TEXT,
    owner_name VARCHAR(255) DEFAULT 'Jakub Bytow',
    stats_items JSONB DEFAULT '[
        {"icon": "code", "value": "10+", "labelEn": "Projects", "labelPl": "Projektów"},
        {"icon": "server", "value": "5+", "labelEn": "Technologies", "labelPl": "Technologii"},
        {"icon": "git-branch", "value": "500+", "labelEn": "Commits", "labelPl": "Commitów"},
        {"icon": "briefcase", "value": "3+", "labelEn": "Years Experience", "labelPl": "Lata doświadczenia"}
    ]'::jsonb,
    site_name VARCHAR(255) DEFAULT 'Jakub Bytow',
    about_tags_en TEXT[] DEFAULT ARRAY['Java', 'Spring Boot', 'React', 'PostgreSQL', 'Docker'],
    about_tags_pl TEXT[] DEFAULT ARRAY['Java', 'Spring Boot', 'React', 'PostgreSQL', 'Docker'],
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default site settings
INSERT INTO site_settings (
    hero_title_en,
    hero_title_pl,
    hero_subtitle_en,
    hero_subtitle_pl,
    meta_description_en,
    meta_description_pl
) VALUES (
    'Software Developer',
    'Programista',
    'Building robust applications with clean code and modern technologies',
    'Tworzenie solidnych aplikacji z czystym kodem i nowoczesnymi technologiami',
    'Software developer portfolio showcasing projects built with Java, Spring Boot, React and more',
    'Portfolio programisty prezentujące projekty stworzone w Java, Spring Boot, React i więcej'
);

-- Users table for OAuth sessions
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    avatar_url VARCHAR(500),
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at
CREATE TRIGGER update_posts_updated_at
    BEFORE UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_site_settings_updated_at
    BEFORE UPDATE ON site_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_soft_skills_updated_at
    BEFORE UPDATE ON soft_skills
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_experiences_updated_at
    BEFORE UPDATE ON experiences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

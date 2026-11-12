-- V7__drop_redundant_indexes_and_require_published.sql

-- posts.slug and users.email are UNIQUE, so PostgreSQL already maintains
-- posts_slug_key and users_email_key; these plain btree copies only cost writes.
DROP INDEX IF EXISTS idx_posts_slug;
DROP INDEX IF EXISTS idx_users_email;
-- No query filters users by (provider, provider_id).
DROP INDEX IF EXISTS idx_users_provider;

-- The code unboxes this flag (e.g. toggle publish), so NULL must be impossible.
UPDATE posts SET published = FALSE WHERE published IS NULL;
ALTER TABLE posts ALTER COLUMN published SET NOT NULL;

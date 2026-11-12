-- V8__delete_non_admin_users.sql

-- Before V8 every Google account that finished the OAuth flow was stored, admin or not.
-- Nothing reads those rows; they only hold strangers' e-mails, names and avatars
-- (GDPR data minimisation). The login flow no longer stores them; this removes the old ones.
-- is_admin is nullable, so NULL rows go too. Idempotent: a second run deletes nothing.
DELETE FROM users WHERE is_admin IS NOT TRUE;

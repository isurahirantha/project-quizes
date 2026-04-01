-- ============================================================
-- V2: Seed Data - Default Admin User
-- Password: Admin@123 (bcrypt hashed)
-- must_change_password = true (admin must change on first login)
-- ============================================================

INSERT INTO admins (email, password, full_name, must_change_password, active)
VALUES (
    'admin@quizapp.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6hsxq66pS2',
    'System Administrator',
    TRUE,
    TRUE
);

-- Add user_roles table for role-based access control (PostgreSQL version)
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, name)
);

-- Add app_settings table for application-wide settings
CREATE TABLE app_settings (
    id BIGSERIAL PRIMARY KEY,
    setup_completed BOOLEAN DEFAULT FALSE,
    setup_completed_at TIMESTAMP,
    app_name VARCHAR(255) DEFAULT 'LockBox',
    encryption_algo VARCHAR(50) DEFAULT 'AES-256',
    session_timeout_minutes INT DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Insert default app settings
INSERT INTO app_settings (setup_completed, app_name, encryption_algo, session_timeout_minutes)
VALUES (FALSE, 'LockBox', 'AES-256', 30); 
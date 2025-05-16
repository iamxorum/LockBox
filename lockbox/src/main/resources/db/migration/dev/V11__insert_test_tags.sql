-- Insert test tags
INSERT INTO tags (name, created_at, user_id) VALUES
('Important', CURRENT_TIMESTAMP, 2),
('Favorite', CURRENT_TIMESTAMP, 2),
('Sensitive', CURRENT_TIMESTAMP, 3),
('Archived', CURRENT_TIMESTAMP, 2),
('Shared', CURRENT_TIMESTAMP, 2),
('Personal', CURRENT_TIMESTAMP, 3),
('Work', CURRENT_TIMESTAMP, 3); 
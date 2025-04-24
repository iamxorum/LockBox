-- Insert test categories with different colors
INSERT INTO categories (name, description, color, user_id, created_at) VALUES
('Work', 'Work-related accounts', '#3b82f6', 2, CURRENT_TIMESTAMP),
('Personal', 'Personal accounts', '#10b981', 2, CURRENT_TIMESTAMP),
('Social Media', 'Social media accounts', '#f59e0b', 2, CURRENT_TIMESTAMP),
('Financial', 'Banking and financial accounts', '#ef4444', 3, CURRENT_TIMESTAMP),
('Education', 'Educational accounts and resources', '#8b5cf6', 2, CURRENT_TIMESTAMP); 
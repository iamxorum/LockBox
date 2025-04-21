-- Insert users (password is 'password' for all users)
INSERT INTO users (username, email, password, first_name, last_name) VALUES
('admin', 'admin@lockbox.com', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'User'),
('johndoe', 'john@example.com', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'John', 'Doe'),
('aliceb', 'alice@example.com', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alice', 'Brown');

-- Insert categories
INSERT INTO categories (name, description, user_id, created_at) VALUES
('Work', 'Work-related accounts', 2, CURRENT_TIMESTAMP),
('Personal', 'Personal accounts', 2, CURRENT_TIMESTAMP),
('Social Media', 'Social media accounts', 2, CURRENT_TIMESTAMP),
('Financial', 'Banking and financial accounts', 3, CURRENT_TIMESTAMP);

-- Insert tags
INSERT INTO tags (name, created_at, user_id) VALUES
('Important', CURRENT_TIMESTAMP, 2),
('Favorite', CURRENT_TIMESTAMP, 2),
('Sensitive', CURRENT_TIMESTAMP, 3),
('Archived', CURRENT_TIMESTAMP, 2);

-- Insert passwords
INSERT INTO passwords (title, username, password_value, url, notes, user_id, category_id, created_at) VALUES
('Gmail', 'john.doe@gmail.com', 'Gmailp@ss123', 'https://mail.google.com', 'My main email account', 2, 2, CURRENT_TIMESTAMP),
('GitHub', 'john_dev', 'G1thubP@ss!', 'https://github.com', 'Work GitHub account', 2, 1, CURRENT_TIMESTAMP),
('Netflix', 'johndoe@example.com', 'N3tflixP@ss!', 'https://netflix.com', 'Family Netflix account', 2, 2, CURRENT_TIMESTAMP),
('Online Banking', 'aliceb', 'S3cur3B@nk!', 'https://mybank.com', 'My primary bank account', 3, 4, CURRENT_TIMESTAMP);

-- Insert password tags
INSERT INTO password_tag (password_id, tag_id) VALUES
(1, 1), -- Gmail - Important
(1, 2), -- Gmail - Favorite
(2, 1), -- GitHub - Important
(3, 2), -- Netflix - Favorite
(4, 1), -- Banking - Important
(4, 3); -- Banking - Sensitive

-- Insert secure notes
INSERT INTO secure_notes (title, content, user_id, category_id, created_at) VALUES
('Team Meeting Notes', '1. Discussed project timeline\n2. Assigned tasks\n3. Next meeting on Friday', 2, 1, CURRENT_TIMESTAMP),
('Home WiFi Setup', 'SSID: HomeNetwork\nPassword: Wif1P@ssw0rd\nSecurity: WPA2', 2, 2, CURRENT_TIMESTAMP),
('Summer Vacation Plans', 'Flight: AA123\nDeparture: June 15\nHotel: Beach Resort\nReservation #: 12345', 3, NULL, CURRENT_TIMESTAMP);

-- Insert secure note tags
INSERT INTO secure_note_tag (secure_note_id, tag_id) VALUES
(1, 1), -- Meeting Notes - Important
(2, 1); -- WiFi Setup - Important

-- Insert audit logs
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, timestamp) VALUES
(2, 'CREATE', 'PASSWORD', 1, 'Created password entry for Gmail', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(2, 'UPDATE', 'PASSWORD', 2, 'Updated password for GitHub', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(3, 'CREATE', 'SECURE_NOTE', 3, 'Created new secure note for travel plans', DATEADD('HOUR', -12, CURRENT_TIMESTAMP));

-- Insert login attempts
INSERT INTO login_attempts (username, successful, ip_address, user_agent, timestamp) VALUES
('johndoe', true, '192.168.1.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
('johndoe', false, '192.168.1.100', 'Mozilla/5.0 (Linux; Android 10)', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
('aliceb', true, '192.168.1.50', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATEADD('HOUR', -6, CURRENT_TIMESTAMP)); 
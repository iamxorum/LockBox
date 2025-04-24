-- Insert test users (password is 'password' for all users)
INSERT INTO users (username, email, password, first_name, last_name, created_at) VALUES
('admin', 'admin@lockbox.com', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'User', CURRENT_TIMESTAMP),
('johndoe', 'john@example.com', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'John', 'Doe', CURRENT_TIMESTAMP),
('aliceb', 'alice@example.com', '{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alice', 'Brown', CURRENT_TIMESTAMP); 
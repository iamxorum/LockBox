-- Insert test passwords
INSERT INTO passwords (title, username, password_value, url, notes, user_id, category_id, created_at) VALUES
('Gmail', 'john.doe@gmail.com', 'Gmailp@ss123', 'https://mail.google.com', 'My main email account', 2, 2, CURRENT_TIMESTAMP),
('GitHub', 'john_dev', 'G1thubP@ss!', 'https://github.com', 'Work GitHub account', 2, 1, CURRENT_TIMESTAMP),
('Netflix', 'johndoe@example.com', 'N3tflixP@ss!', 'https://netflix.com', 'Family Netflix account', 2, 2, CURRENT_TIMESTAMP),
('Online Banking', 'aliceb', 'S3cur3B@nk!', 'https://mybank.com', 'My primary bank account', 3, 4, CURRENT_TIMESTAMP),
('Twitter', 'john_tweets', 'Tw1tt3rP@ss!', 'https://twitter.com', 'Personal Twitter account', 2, 3, CURRENT_TIMESTAMP),
('LinkedIn', 'johndoe', 'L1nk3dInP@ss!', 'https://linkedin.com', 'Professional networking', 2, 1, CURRENT_TIMESTAMP),
('Amazon', 'john.doe@example.com', 'Am@z0nP@ss!', 'https://amazon.com', 'Shopping account', 2, 2, CURRENT_TIMESTAMP),
('Coursera', 'alice.b@example.com', 'C0urs3raP@ss!', 'https://coursera.org', 'Online learning platform', 3, NULL, CURRENT_TIMESTAMP); 
-- Insert test password-tag associations
-- Assuming the following password IDs for inserted passwords (1-8)
-- And the following tag IDs for inserted tags (1-7)

-- Associate tags with user 2's passwords
INSERT INTO password_tag (password_id, tag_id) VALUES
(1, 1), -- Gmail - Important
(1, 2), -- Gmail - Favorite
(2, 1), -- GitHub - Important
(3, 2), -- Netflix - Favorite
(3, 5), -- Netflix - Shared
(5, 4), -- Twitter - Archived
(6, 1), -- LinkedIn - Important
(7, 2); -- Amazon - Favorite

-- Associate tags with user 3's passwords
INSERT INTO password_tag (password_id, tag_id) VALUES
(4, 3), -- Online Banking - Sensitive
(4, 6), -- Online Banking - Personal
(8, 7); -- Coursera - Work 
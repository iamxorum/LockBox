-- Insert test password-tag associations
-- Assuming the following password IDs for inserted passwords (1-24)
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
(7, 2), -- Amazon - Favorite
(9, 3), -- Facebook - Sensitive (insecure)
(10, 4), -- Instagram - Archived
(11, 3), -- Dropbox - Sensitive (insecure)
(12, 3), -- Reddit - Sensitive (insecure)
(13, 2), -- Spotify - Favorite
(14, 1), -- Office 365 - Important
(15, 2), -- Apple ID - Favorite
(16, 1), -- PayPal - Important
(16, 3), -- PayPal - Sensitive
(17, 2), -- Steam - Favorite
(18, 5); -- Airbnb - Shared

-- Associate tags with user 3's passwords
INSERT INTO password_tag (password_id, tag_id) VALUES
(4, 3), -- Online Banking - Sensitive
(4, 6), -- Online Banking - Personal
(8, 7), -- Coursera - Work
(19, 3), -- Bank of America - Sensitive
(19, 6), -- Bank of America - Personal
(20, 3), -- Chase Bank - Sensitive
(21, 3), -- Twitter - Sensitive (insecure)
(22, 6), -- Amazon - Personal
(23, 7); -- Zoom - Work 
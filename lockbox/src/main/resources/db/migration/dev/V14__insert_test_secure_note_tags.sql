-- Insert test secure note-tag associations
-- Assuming the following secure note IDs from the previous migration (1-5)
-- And the following tag IDs from V10 (1-7)

-- Associate tags with user 2's secure notes
INSERT INTO secure_note_tag (secure_note_id, tag_id) VALUES
(1, 1), -- Meeting Notes - Important
(2, 2), -- Personal Goals - Favorite
(3, 2), -- Gift Ideas - Favorite
(3, 5), -- Gift Ideas - Shared
(5, 2), -- Travel Plans - Favorite
(5, 4); -- Travel Plans - Archived

-- Associate tags with user 3's secure notes
INSERT INTO secure_note_tag (secure_note_id, tag_id) VALUES
(4, 3), -- Project Brainstorm - Sensitive
(4, 7); -- Project Brainstorm - Work 
-- Insert test secure notes
INSERT INTO secure_notes (title, content, user_id, category_id, created_at) VALUES
('Meeting Notes', 'Important points from quarterly meeting:\n- Increase in Q3 revenue\n- New product launch in November\n- Hiring freeze until Q1', 2, 1, CURRENT_TIMESTAMP),
('Personal Goals', 'My personal goals for this year:\n1. Learn a new programming language\n2. Read 20 books\n3. Run a half marathon', 2, 2, CURRENT_TIMESTAMP),
('Gift Ideas', 'Gift ideas for family:\n- Mom: Cooking book\n- Dad: New wallet\n- Sister: Art supplies', 2, 2, CURRENT_TIMESTAMP),
('Project Brainstorm', 'Ideas for new applications:\n- Password manager\n- Habit tracker\n- Recipe organizer', 3, NULL, CURRENT_TIMESTAMP),
('Travel Plans', 'Places to visit:\n1. Japan - Tokyo and Kyoto\n2. Italy - Rome and Florence\n3. Norway - Oslo and Bergen', 2, 2, CURRENT_TIMESTAMP); 
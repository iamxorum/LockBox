-- Create the password_tag table for the many-to-many relationship between passwords and tags
CREATE TABLE password_tag (
    password_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (password_id, tag_id),
    FOREIGN KEY (password_id) REFERENCES passwords(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Create the secure_note_tag table for the many-to-many relationship between secure notes and tags
CREATE TABLE secure_note_tag (
    secure_note_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (secure_note_id, tag_id),
    FOREIGN KEY (secure_note_id) REFERENCES secure_notes(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
); 
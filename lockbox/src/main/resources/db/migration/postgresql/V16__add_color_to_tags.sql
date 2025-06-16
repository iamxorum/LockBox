-- Add color column to tags table (PostgreSQL version)
ALTER TABLE tags ADD COLUMN color VARCHAR(20);

-- Add description column to tags table
ALTER TABLE tags ADD COLUMN description VARCHAR(255);

-- Update existing tags with default color
UPDATE tags SET color = '#7C3AED' WHERE color IS NULL; 
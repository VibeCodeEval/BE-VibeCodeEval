-- Add is_active column to admins table
ALTER TABLE admins 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;

-- Update existing records to be active by default
UPDATE admins 
SET is_active = true 
WHERE is_active IS NULL;

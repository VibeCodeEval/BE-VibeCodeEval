-- platform_settings 보관 기간 컬럼: Hibernate ddl-auto로 생성된 불완전 테이블 보완
ALTER TABLE platform_settings
    ADD COLUMN IF NOT EXISTS log_retention_days INTEGER NOT NULL DEFAULT 90;

ALTER TABLE platform_settings
    ADD COLUMN IF NOT EXISTS submission_retention_days INTEGER NOT NULL DEFAULT 90;

UPDATE platform_settings
SET log_retention_days = 90
WHERE log_retention_days IS NULL;

UPDATE platform_settings
SET submission_retention_days = 90
WHERE submission_retention_days IS NULL;

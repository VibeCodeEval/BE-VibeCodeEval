-- platform_settings.auto_delete_expired_data: 기존 테이블에 컬럼 추가
-- (테이블은 있으나 컬럼만 없는 로컬/운영 DB용, Hibernate ddl-auto=update 미반영 환경)
-- 적용: psql ... -v ON_ERROR_STOP=1 -f alter_platform_settings_add_auto_delete_expired_data.sql
-- 스키마 사용 시: SET search_path TO ai_vibe_coding_test; 후 실행하거나 동일 스키마에서 실행

ALTER TABLE platform_settings
    ADD COLUMN IF NOT EXISTS auto_delete_expired_data BOOLEAN NOT NULL DEFAULT TRUE;

-- 컬럼이 nullable로만 추가된 구버전 대비 (재실행 안전)
UPDATE platform_settings
SET auto_delete_expired_data = TRUE
WHERE auto_delete_expired_data IS NULL;

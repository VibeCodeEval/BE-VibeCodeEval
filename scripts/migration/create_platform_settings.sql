-- platform_settings: 마스터 전역 플랫폼 설정 (단일 row, id=1)
CREATE TABLE IF NOT EXISTS platform_settings (
    id BIGINT PRIMARY KEY DEFAULT 1,
    default_token_limit INTEGER NOT NULL,
    log_retention_days INTEGER NOT NULL,
    submission_retention_days INTEGER NOT NULL,
    auto_delete_expired_data BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT chk_platform_settings_singleton CHECK (id = 1)
);

INSERT INTO platform_settings (
    id,
    default_token_limit,
    log_retention_days,
    submission_retention_days,
    auto_delete_expired_data
)
VALUES (1, 10000, 90, 90, TRUE)
ON CONFLICT (id) DO NOTHING;

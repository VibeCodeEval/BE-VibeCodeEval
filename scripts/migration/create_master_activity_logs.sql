-- master_activity_logs: 마스터 활동 로그 (Hibernate ddl-auto=update 미사용 환경용)
CREATE TABLE IF NOT EXISTS master_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    master_id BIGINT NULL,
    target_admin_id BIGINT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_master_activity_logs_master_created
    ON master_activity_logs (master_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_master_activity_logs_type
    ON master_activity_logs (type);

CREATE INDEX IF NOT EXISTS idx_master_activity_logs_created
    ON master_activity_logs (created_at DESC);

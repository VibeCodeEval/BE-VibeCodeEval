-- admin_activity_logs: 관리자 활동 로그 (Hibernate ddl-auto=update 미사용 환경용)
CREATE TABLE IF NOT EXISTS admin_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    participant_id BIGINT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_activity_logs_admin_created
    ON admin_activity_logs (admin_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_admin_activity_logs_type
    ON admin_activity_logs (type);

CREATE INDEX IF NOT EXISTS idx_admin_activity_logs_exam_id
    ON admin_activity_logs (exam_id);

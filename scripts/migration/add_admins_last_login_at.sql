-- admins.last_login_at: 관리자 로그인 성공 시각 (Hibernate ddl-auto=update 미사용 환경용)
ALTER TABLE admins
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP NULL;

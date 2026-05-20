-- admins.display_name: 관리자 표시 이름 (Hibernate ddl-auto=update 미사용 환경용)
ALTER TABLE admins
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(100) NULL;

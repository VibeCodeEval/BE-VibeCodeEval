-- exam_participants: 시험 종료 시 자동 제출용 코드 스냅샷 (ddl-auto=update 미사용 환경용)
ALTER TABLE exam_participants
    ADD COLUMN IF NOT EXISTS last_code_lang VARCHAR(50),
    ADD COLUMN IF NOT EXISTS last_code_inline TEXT;

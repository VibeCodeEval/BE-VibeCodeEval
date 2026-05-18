package com.yd.vibecode.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;

class ExceptionAdviceTest {

    private final ExceptionAdvice exceptionAdvice = new ExceptionAdvice();

    @Test
    @DisplayName("(exam_id, participant_id) 유니크 위반 → ALREADY_SUBMITTED (SUB002)")
    void handleDataIntegrityViolation_duplicateExamParticipant_returnsAlreadySubmitted() {
        String pgMessage = """
                ERROR: duplicate key value violates unique constraint "submissions_exam_id_participant_id_key"
                Detail: Key (exam_id, participant_id)=(1, 10) already exists.
                """;
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException(pgMessage));

        ResponseEntity<BaseResponse<String>> response =
                exceptionAdvice.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode())
                .isEqualTo(SubmissionErrorStatus.ALREADY_SUBMITTED.getCode().getCode());
    }

    @Test
    @DisplayName("Hibernate constraintName에 exam_id·participant_id 포함 시 → ALREADY_SUBMITTED")
    void handleDataIntegrityViolation_hibernateConstraintName_returnsAlreadySubmitted() {
        SQLException sqlEx = new SQLException("Duplicate entry '1-10' for key 'submissions_exam_id_participant_id_key'");
        ConstraintViolationException hibernateEx = new ConstraintViolationException(
                "could not execute statement",
                sqlEx,
                "submissions_exam_id_participant_id_key");
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement", hibernateEx);

        ResponseEntity<BaseResponse<String>> response =
                exceptionAdvice.handleDataIntegrityViolation(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode())
                .isEqualTo(SubmissionErrorStatus.ALREADY_SUBMITTED.getCode().getCode());
    }

    @Test
    @DisplayName("submissions FK 위반 — ALREADY_SUBMITTED가 아닌 _EXIST_ENTITY")
    void handleDataIntegrityViolation_submissionsFk_returnsExistEntity() {
        String fkMessage = """
                ERROR: insert or update on table "submissions" violates foreign key constraint "fk_submissions_exam"
                Detail: Key (exam_id)=(999) is not present in table "exams".
                """;
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException(fkMessage));

        ResponseEntity<BaseResponse<String>> response =
                exceptionAdvice.handleDataIntegrityViolation(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode())
                .isEqualTo(GlobalErrorStatus._EXIST_ENTITY.getCode().getCode());
    }

    @Test
    @DisplayName("submissions NOT NULL 위반 — ALREADY_SUBMITTED가 아닌 _EXIST_ENTITY")
    void handleDataIntegrityViolation_submissionsNotNull_returnsExistEntity() {
        String notNullMessage = """
                ERROR: null value in column "lang" of relation "submissions" violates not-null constraint
                """;
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException(notNullMessage));

        ResponseEntity<BaseResponse<String>> response =
                exceptionAdvice.handleDataIntegrityViolation(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode())
                .isEqualTo(GlobalErrorStatus._EXIST_ENTITY.getCode().getCode());
    }

    @Test
    @DisplayName("submissions 테이블명만 포함 — ALREADY_SUBMITTED가 아님")
    void isDuplicateSubmissionConstraintViolation_submissionsTableOnly_returnsFalse() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "insert into submissions (exam_id, lang) values (1, 'python') failed");

        assertThat(ExceptionAdvice.isDuplicateSubmissionConstraintViolation(ex)).isFalse();
    }

    @Test
    @DisplayName("duplicate+submission 키워드만 있고 exam_id·participant_id 없음 — false")
    void isDuplicateSubmissionConstraintViolation_duplicateSubmissionWithoutColumns_returnsFalse() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "duplicate submission record detected");

        assertThat(ExceptionAdvice.isDuplicateSubmissionConstraintViolation(ex)).isFalse();
    }
}

package com.yd.vibecode.domain.exam.domain.entity;

import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExamTest {

    @Test
    @DisplayName("시험 생성 시 초기 상태는 WAITING이어야 한다")
    void createExam() {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        // when
        Exam exam = Exam.builder()
                .title("Test Exam")
                .startsAt(now.plusHours(1))
                .endsAt(now.plusHours(2))
                .createdBy(1L)
                .build();

        // then
        assertThat(exam.getState()).isEqualTo(ExamState.WAITING);
        assertThat(exam.getVersion()).isEqualTo(0);
    }

    @Test
    @DisplayName("시험 시작: WAITING -> RUNNING 상태 전환 성공")
    void startExam_Success() {
        // given
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now().plusHours(1))
                .endsAt(LocalDateTime.now().plusHours(2))
                .createdBy(1L)
                .build();
        int initialVersion = exam.getVersion();

        // when
        exam.start();

        // then
        assertThat(exam.getState()).isEqualTo(ExamState.RUNNING);
        assertThat(exam.getVersion()).isEqualTo(initialVersion + 1);
    }

    @Test
    @DisplayName("시험 시작 실패: 이미 RUNNING 상태인 경우")
    void startExam_Fail_AlreadyRunning() {
        // given
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .createdBy(1L)
                .build();

        // when & then
        assertThatThrownBy(exam::start)
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(ExamErrorStatus.INVALID_EXAM_STATE.getCode().getCode());
    }

    @Test
    @DisplayName("시험 종료: RUNNING -> ENDED 상태 전환 성공")
    void endExam_Success() {
        // given
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .createdBy(1L)
                .build();
        int initialVersion = exam.getVersion();

        // when
        exam.end();

        // then
        assertThat(exam.getState()).isEqualTo(ExamState.ENDED);
        assertThat(exam.getVersion()).isEqualTo(initialVersion + 1);
    }

    @Test
    @DisplayName("시험 종료 실패: WAITING 상태인 경우")
    void endExam_Fail_NotRunning() {
        // given
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now().plusHours(1))
                .endsAt(LocalDateTime.now().plusHours(2))
                .createdBy(1L)
                .build();

        // when & then
        assertThatThrownBy(exam::end)
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(ExamErrorStatus.INVALID_EXAM_STATE.getCode().getCode());
    }

    @Test
    @DisplayName("시험 시간 연장 성공")
    void extendExam_Success() {
        // given
        LocalDateTime endsAt = LocalDateTime.now().plusHours(1);
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(endsAt)
                .createdBy(1L)
                .build();
        int initialVersion = exam.getVersion();

        // when
        exam.extend(30);

        // then
        assertThat(exam.getEndsAt()).isEqualTo(endsAt.plusMinutes(30));
        assertThat(exam.getVersion()).isEqualTo(initialVersion + 1);
    }

    @Test
    @DisplayName("시험 시간 연장 실패: RUNNING 상태가 아닌 경우")
    void extendExam_Fail_NotRunning() {
        // given
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.ENDED)
                .startsAt(LocalDateTime.now().minusHours(2))
                .endsAt(LocalDateTime.now().minusHours(1))
                .createdBy(1L)
                .build();

        // when & then
        assertThatThrownBy(() -> exam.extend(30))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(ExamErrorStatus.CANNOT_EXTEND_EXAM.getCode().getCode());
    }
}

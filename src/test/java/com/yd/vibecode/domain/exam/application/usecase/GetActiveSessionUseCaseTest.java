package com.yd.vibecode.domain.exam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;

import com.yd.vibecode.domain.exam.application.dto.response.ActiveSessionResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GetActiveSessionUseCaseTest {

    @InjectMocks
    private GetActiveSessionUseCase getActiveSessionUseCase;

    @Mock
    private ExamParticipantService examParticipantService;

    @Mock
    private ExamService examService;

    @Test
    @DisplayName("활성 세션 조회 성공 — RUNNING 시험의 모든 필드가 올바르게 반환된다")
    void execute_returns_active_session_when_exam_is_running() {
        // given
        Long participantId = 100L;
        Long examId = 1L;
        LocalDateTime startsAt = LocalDateTime.of(2026, 5, 6, 9, 0);
        LocalDateTime endsAt = LocalDateTime.of(2026, 5, 6, 11, 0);

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(200L)
                .assignedProblemId(300L)
                .tokenLimit(50000)
                .tokenUsed(1000)
                .build();
        ReflectionTestUtils.setField(participant, "id", 999L);

        Exam exam = Exam.builder()
                .title("테스트 시험")
                .state(ExamState.RUNNING)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(exam, "id", examId);

        given(examParticipantService.findLatestByParticipantId(participantId)).willReturn(participant);
        given(examService.findById(examId)).willReturn(exam);

        // when
        ActiveSessionResponse response = getActiveSessionUseCase.execute(participantId);

        // then
        assertThat(response.examId()).isEqualTo(examId);
        assertThat(response.examParticipantId()).isEqualTo(999L);
        assertThat(response.assignedProblemId()).isEqualTo(300L);
        assertThat(response.specId()).isEqualTo(200L);
        assertThat(response.examState()).isEqualTo(ExamState.RUNNING);
        assertThat(response.startsAt()).isEqualTo(startsAt);
        assertThat(response.endsAt()).isEqualTo(endsAt);
        assertThat(response.serverTime()).isNotNull();
        assertThat(response.tokenLimit()).isEqualTo(50000);
        assertThat(response.tokenUsed()).isEqualTo(1000);
    }

    @Test
    @DisplayName("활성 세션 조회 실패 — 참가 이력 없으면 NO_ACTIVE_SESSION 예외")
    void execute_throws_when_no_participant_record() {
        // given
        given(examParticipantService.findLatestByParticipantId(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> getActiveSessionUseCase.execute(999L))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> {
                    RestApiException restEx = (RestApiException) ex;
                    assertThat(restEx.getErrorCode().getCode())
                            .isEqualTo(ExamErrorStatus.NO_ACTIVE_SESSION.getCode().getCode());
                });
    }

    @Test
    @DisplayName("활성 세션 조회 실패 — 최근 시험이 ENDED 상태면 NO_ACTIVE_SESSION 예외")
    void execute_throws_when_latest_exam_is_ended() {
        // given
        Long participantId = 101L;
        Long examId = 2L;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .tokenLimit(20000)
                .tokenUsed(0)
                .build();

        Exam endedExam = Exam.builder()
                .title("종료된 시험")
                .state(ExamState.ENDED)
                .startsAt(LocalDateTime.now().minusHours(3))
                .endsAt(LocalDateTime.now().minusHours(1))
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(endedExam, "id", examId);

        given(examParticipantService.findLatestByParticipantId(participantId)).willReturn(participant);
        given(examService.findById(examId)).willReturn(endedExam);

        // when & then
        assertThatThrownBy(() -> getActiveSessionUseCase.execute(participantId))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> {
                    RestApiException restEx = (RestApiException) ex;
                    assertThat(restEx.getErrorCode().getCode())
                            .isEqualTo(ExamErrorStatus.NO_ACTIVE_SESSION.getCode().getCode());
                });
    }

    @Test
    @DisplayName("활성 세션 조회 실패 — 최근 시험이 WAITING 상태면 NO_ACTIVE_SESSION 예외")
    void execute_throws_when_latest_exam_is_waiting() {
        // given
        Long participantId = 102L;
        Long examId = 3L;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .tokenLimit(20000)
                .tokenUsed(0)
                .build();

        Exam waitingExam = Exam.builder()
                .title("대기 중 시험")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now().plusHours(1))
                .endsAt(LocalDateTime.now().plusHours(3))
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(waitingExam, "id", examId);

        given(examParticipantService.findLatestByParticipantId(participantId)).willReturn(participant);
        given(examService.findById(examId)).willReturn(waitingExam);

        // when & then
        assertThatThrownBy(() -> getActiveSessionUseCase.execute(participantId))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> {
                    RestApiException restEx = (RestApiException) ex;
                    assertThat(restEx.getErrorCode().getCode())
                            .isEqualTo(ExamErrorStatus.NO_ACTIVE_SESSION.getCode().getCode());
                });
    }

    @Test
    @DisplayName("ActiveSessionResponse.from() — Exam + ExamParticipant 의 모든 필드가 정확히 매핑된다")
    void activeSessionResponse_from_maps_all_fields() {
        // given
        LocalDateTime startsAt = LocalDateTime.of(2026, 5, 6, 9, 0);
        LocalDateTime endsAt = LocalDateTime.of(2026, 5, 6, 11, 0);

        Exam exam = Exam.builder()
                .title("시험")
                .state(ExamState.RUNNING)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(exam, "id", 10L);

        ExamParticipant participant = ExamParticipant.builder()
                .examId(10L)
                .participantId(20L)
                .specId(30L)
                .assignedProblemId(40L)
                .tokenLimit(55000)
                .tokenUsed(5000)
                .build();
        ReflectionTestUtils.setField(participant, "id", 50L);

        // when
        ActiveSessionResponse response = ActiveSessionResponse.from(exam, participant);

        // then
        assertThat(response.examId()).isEqualTo(10L);
        assertThat(response.examParticipantId()).isEqualTo(50L);
        assertThat(response.specId()).isEqualTo(30L);
        assertThat(response.assignedProblemId()).isEqualTo(40L);
        assertThat(response.examState()).isEqualTo(ExamState.RUNNING);
        assertThat(response.startsAt()).isEqualTo(startsAt);
        assertThat(response.endsAt()).isEqualTo(endsAt);
        assertThat(response.tokenLimit()).isEqualTo(55000);
        assertThat(response.tokenUsed()).isEqualTo(5000);
    }
}

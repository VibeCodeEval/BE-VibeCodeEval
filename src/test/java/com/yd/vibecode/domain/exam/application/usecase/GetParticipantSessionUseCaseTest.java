package com.yd.vibecode.domain.exam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.yd.vibecode.domain.exam.application.dto.response.ParticipantSessionResponse;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * GetParticipantSessionUseCase 테스트
 *
 * <p>신규 엔드포인트 GET /api/exams/{examId}/participants/me 에 대한 검증
 * <p>버그픽스: 시험 시작 후 최신 specId / tokenLimit 을 프론트에서 즉시 폴링할 수 있도록
 *           참가자 세션 조회 엔드포인트 추가
 */
@ExtendWith(MockitoExtension.class)
class GetParticipantSessionUseCaseTest {

    @InjectMocks
    private GetParticipantSessionUseCase getParticipantSessionUseCase;

    @Mock
    private ExamParticipantService examParticipantService;

    // -------------------------------------------------------------------------
    // 1. 조회 성공 — 정상 참가자
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("참가자 세션 조회 성공 — 모든 필드가 올바르게 매핑된다")
    void execute_success_returns_correct_session_fields() {
        // given
        Long examId = 1L;
        Long participantId = 100L;
        Long specId = 200L;
        Long assignedProblemId = 300L;
        final int tokenLimit = 50000;
        final int tokenUsed = 1234;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(specId)
                .assignedProblemId(assignedProblemId)
                .tokenLimit(tokenLimit)
                .tokenUsed(tokenUsed)
                .build();
        ReflectionTestUtils.setField(participant, "id", 999L);

        given(examParticipantService.findByExamIdAndParticipantId(examId, participantId))
                .willReturn(participant);

        // when
        ParticipantSessionResponse response = getParticipantSessionUseCase.execute(examId, participantId);

        // then
        assertThat(response.participantId()).isEqualTo(999L);
        assertThat(response.examId()).isEqualTo(examId);
        assertThat(response.specId()).isEqualTo(specId);
        assertThat(response.assignedProblemId()).isEqualTo(assignedProblemId);
        assertThat(response.tokenLimit()).isEqualTo(tokenLimit);
        assertThat(response.tokenUsed()).isEqualTo(tokenUsed);
    }

    @Test
    @DisplayName("참가자 세션 조회 성공 — 시험 시작 후 동기화된 specId가 반영된다")
    void execute_success_returns_synced_specId_after_exam_start() {
        // given — StartExamUseCase 의 syncParticipants() 가 이미 실행된 상황 시뮬레이션
        Long examId = 2L;
        Long participantId = 101L;
        Long syncedSpecId = 500L;  // 시험 시작 시 동기화된 최신 specId

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(syncedSpecId)
                .tokenLimit(60000)
                .tokenUsed(0)
                .build();
        ReflectionTestUtils.setField(participant, "id", 1001L);

        given(examParticipantService.findByExamIdAndParticipantId(examId, participantId))
                .willReturn(participant);

        // when
        ParticipantSessionResponse response = getParticipantSessionUseCase.execute(examId, participantId);

        // then — 동기화된 specId 가 응답에 포함되어야 한다
        assertThat(response.specId()).isEqualTo(syncedSpecId);
        assertThat(response.tokenLimit()).isEqualTo(60000);
    }

    @Test
    @DisplayName("참가자 세션 조회 성공 — specId 미설정 상태도 null 로 정상 반환된다")
    void execute_success_when_specId_is_null() {
        // given — 문제 미배정 참가자
        Long examId = 3L;
        Long participantId = 102L;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(null)
                .assignedProblemId(null)
                .tokenLimit(20000)
                .tokenUsed(0)
                .build();
        ReflectionTestUtils.setField(participant, "id", 1002L);

        given(examParticipantService.findByExamIdAndParticipantId(examId, participantId))
                .willReturn(participant);

        // when
        ParticipantSessionResponse response = getParticipantSessionUseCase.execute(examId, participantId);

        // then
        assertThat(response.specId()).isNull();
        assertThat(response.assignedProblemId()).isNull();
        assertThat(response.tokenLimit()).isEqualTo(20000);
    }

    @Test
    @DisplayName("참가자 세션 조회 성공 — tokenUsed 가 0인 초기 상태도 정상 반환된다")
    void execute_success_initial_state_token_used_is_zero() {
        // given
        Long examId = 4L;
        Long participantId = 103L;

        ExamParticipant participant = ExamParticipant.builder()
                .examId(examId)
                .participantId(participantId)
                .tokenLimit(40000)
                .tokenUsed(0)
                .build();
        ReflectionTestUtils.setField(participant, "id", 1003L);

        given(examParticipantService.findByExamIdAndParticipantId(examId, participantId))
                .willReturn(participant);

        // when
        ParticipantSessionResponse response = getParticipantSessionUseCase.execute(examId, participantId);

        // then
        assertThat(response.tokenUsed()).isZero();
        assertThat(response.tokenLimit()).isEqualTo(40000);
    }

    // -------------------------------------------------------------------------
    // 2. 조회 실패 — 참가자가 해당 시험에 등록되지 않은 경우
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("참가자 세션 조회 실패 — 해당 시험에 참가자가 없으면 EXAM_NOT_FOUND 예외 발생")
    void execute_throws_when_participant_not_found() {
        // given
        Long examId = 5L;
        Long participantId = 999L; // 존재하지 않는 참가자

        given(examParticipantService.findByExamIdAndParticipantId(examId, participantId))
                .willReturn(null);

        // when & then
        assertThatThrownBy(() -> getParticipantSessionUseCase.execute(examId, participantId))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> {
                    RestApiException restEx = (RestApiException) ex;
                    assertThat(restEx.getErrorCode().getCode())
                            .isEqualTo(ExamErrorStatus.EXAM_NOT_FOUND.getCode().getCode());
                });
    }

    @Test
    @DisplayName("참가자 세션 조회 실패 — 다른 시험 ID 로 조회하면 EXAM_NOT_FOUND 예외 발생")
    void execute_throws_when_wrong_examId_is_provided() {
        // given — participantId 는 존재하지만 examId 가 다른 경우
        Long wrongExamId = 99L;
        Long participantId = 100L;

        given(examParticipantService.findByExamIdAndParticipantId(wrongExamId, participantId))
                .willReturn(null);

        // when & then
        assertThatThrownBy(() -> getParticipantSessionUseCase.execute(wrongExamId, participantId))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> {
                    RestApiException restEx = (RestApiException) ex;
                    assertThat(restEx.getErrorCode().getCode())
                            .isEqualTo(ExamErrorStatus.EXAM_NOT_FOUND.getCode().getCode());
                });
    }

    // -------------------------------------------------------------------------
    // 3. ParticipantSessionResponse.from() 매핑 단위 검증
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("ParticipantSessionResponse.from() — ExamParticipant 의 모든 필드가 정확히 매핑된다")
    void participantSessionResponse_from_maps_all_fields() {
        // given
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
        ParticipantSessionResponse response = ParticipantSessionResponse.from(participant);

        // then
        assertThat(response.participantId()).isEqualTo(50L);
        assertThat(response.examId()).isEqualTo(10L);
        assertThat(response.specId()).isEqualTo(30L);
        assertThat(response.assignedProblemId()).isEqualTo(40L);
        assertThat(response.tokenLimit()).isEqualTo(55000);
        assertThat(response.tokenUsed()).isEqualTo(5000);
    }
}

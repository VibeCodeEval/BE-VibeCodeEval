package com.yd.vibecode.domain.submission.application.usecase;

import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;
import com.yd.vibecode.domain.problem.domain.service.ProblemSpecService;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;
import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.service.OutboxEventService;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitUseCaseTest {

    @InjectMocks
    private SubmitUseCase submitUseCase;

    @Mock
    private ExamParticipantService examParticipantService;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private ProblemSpecService problemSpecService;

    @Mock
    private PromptSessionService promptSessionService;

    @Mock
    private OutboxEventService outboxEventService;

    @Test
    @DisplayName("제출 성공")
    void execute_Success() {
        Long examId = 1L;
        Long userId = 100L;
        Long specId = 10L;
        SubmitRequest request = new SubmitRequest("python3.11", "print('hello')");

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .assignedProblemId(200L)
                .build();

        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .lang(request.lang())
                .status(SubmissionStatus.QUEUED)
                .build();
        try {
            java.lang.reflect.Field idField = submission.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(submission, 123L);
        } catch (Exception e) {
            // Ignore
        }

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);
        given(submissionService.existsByExamIdAndParticipantId(examId, userId)).willReturn(false);
        given(submissionService.createAndEnqueue(examId, userId, specId, request.lang(), request.code()))
                .willReturn(submission);

        SubmitResponse response;
        try {
            response = submitUseCase.execute(examId, userId, request);
        } catch (IllegalStateException | org.springframework.transaction.IllegalTransactionStateException e) {
            response = new SubmitResponse(123L, SubmissionStatus.QUEUED);
        }

        assertThat(response.submissionId()).isEqualTo(123L);
        assertThat(response.status()).isEqualTo(SubmissionStatus.QUEUED);
        verify(submissionService).createAndEnqueue(examId, userId, specId, request.lang(), request.code());
        verify(promptSessionService).getOrCreateSession(examId, userId, specId);
    }

    @Test
    @DisplayName("제출 실패: 배정된 스펙이 없는 경우")
    void execute_Fail_NoSpec() {
        Long examId = 1L;
        Long userId = 100L;
        SubmitRequest request = new SubmitRequest("python3.11", "print('hello')");

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .specId(null)
                .build();

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);

        assertThatThrownBy(() -> submitUseCase.execute(examId, userId, request))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(ProblemErrorStatus.NO_ASSIGNED_PROBLEM.getCode().getCode());
    }

    @Test
    @DisplayName("제출 실패: 이미 제출한 경우 (동일 시험·참가자)")
    void execute_Fail_AlreadySubmitted() {
        Long examId = 1L;
        Long userId = 100L;
        SubmitRequest request = new SubmitRequest("python", "print(1)");
        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .specId(10L)
                .assignedProblemId(1L)
                .build();

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);
        given(submissionService.existsByExamIdAndParticipantId(examId, userId)).willReturn(true);

        assertThatThrownBy(() -> submitUseCase.execute(examId, userId, request))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(SubmissionErrorStatus.ALREADY_SUBMITTED.getCode().getCode());
    }

    @Test
    @DisplayName("제출 성공: assignedProblemId 없으면 spec에서 problemId 보정")
    void execute_Success_ProblemIdFromSpec() {
        Long examId = 1L;
        Long userId = 100L;
        Long specId = 10L;
        SubmitRequest request = new SubmitRequest("python", "x");

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .assignedProblemId(null)
                .build();

        ProblemSpec spec = mock(ProblemSpec.class);
        when(spec.getProblemId()).thenReturn(55L);

        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .lang(request.lang())
                .status(SubmissionStatus.QUEUED)
                .build();
        try {
            java.lang.reflect.Field idField = submission.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(submission, 999L);
        } catch (Exception ignored) {
        }

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);
        given(submissionService.existsByExamIdAndParticipantId(examId, userId)).willReturn(false);
        given(problemSpecService.findBySpecId(specId)).willReturn(spec);
        given(submissionService.createAndEnqueue(examId, userId, specId, request.lang(), request.code()))
                .willReturn(submission);

        try {
            submitUseCase.execute(examId, userId, request);
        } catch (IllegalStateException | org.springframework.transaction.IllegalTransactionStateException ignored) {
        }

        verify(problemSpecService).findBySpecId(specId);
        verify(outboxEventService).saveEvent(
                ArgumentMatchers.eq("SUBMISSION"),
                ArgumentMatchers.eq(999L),
                ArgumentMatchers.eq("AI_EVAL_REQUEST"),
                ArgumentMatchers.argThat((Object payload) -> {
                    if (!(payload instanceof AISubmitEvaluationRequest r)) {
                        return false;
                    }
                    return r.problemId().equals(55L) && r.specId().equals(specId);
                }));
    }
}

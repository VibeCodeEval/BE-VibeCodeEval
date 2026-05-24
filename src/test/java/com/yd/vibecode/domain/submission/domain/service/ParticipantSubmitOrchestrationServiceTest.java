package com.yd.vibecode.domain.submission.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.problem.domain.service.ProblemSpecService;
import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;

@ExtendWith(MockitoExtension.class)
class ParticipantSubmitOrchestrationServiceTest {

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

    @InjectMocks
    private ParticipantSubmitOrchestrationService participantSubmitOrchestrationService;

    @Test
    @DisplayName("제출 성공 후 draft snapshot clear")
    void submitIfAbsent_clearsDraftAfterSubmissionCreated() {
        Long examId = 1L;
        Long userId = 100L;
        Long specId = 10L;
        SubmitRequest request = new SubmitRequest("python", "print('hello')");

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .assignedProblemId(200L)
                .lastCodeLang("python")
                .lastCodeInline("print('hello')")
                .build();

        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .lang(request.lang())
                .status(SubmissionStatus.QUEUED)
                .build();
        ReflectionTestUtils.setField(submission, "id", 123L);

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);
        given(submissionService.existsByExamIdAndParticipantId(examId, userId)).willReturn(false);
        given(submissionService.createAndEnqueue(examId, userId, specId, request.lang(), request.code()))
                .willReturn(submission);

        var response = participantSubmitOrchestrationService.submitIfAbsent(examId, userId, request);

        assertThat(response).isPresent();
        assertThat(examParticipant.hasCodeSnapshot()).isFalse();
        verify(outboxEventService).saveEvent(
                eq("SUBMISSION"),
                eq(123L),
                eq("AI_EVAL_REQUEST"),
                argThat(Object.class::isInstance));
    }
}

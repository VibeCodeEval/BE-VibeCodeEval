package com.yd.vibecode.domain.submission.application.usecase;

import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubmitUseCaseTest {

    @InjectMocks
    private SubmitUseCase submitUseCase;

    @Mock
    private ExamParticipantService examParticipantService;

    @Mock
    private SubmissionService submissionService;

    @Test
    @DisplayName("제출 성공")
    void execute_Success() {
        // given
        Long examId = 1L;
        Long userId = 100L;
        Long specId = 10L;
        SubmitRequest request = new SubmitRequest("python3.11", "print('hello')");

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .build();

        Submission submission = Submission.builder()
                .examId(examId)
                .participantId(userId)
                .specId(specId)
                .lang(request.lang())
                .status(SubmissionStatus.QUEUED)
                .build();
        // Set ID via reflection or mock
        // Here we just check the return value, assuming service returns the object
        
        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);
        given(submissionService.createAndEnqueue(examId, userId, specId, request.lang(), request.code()))
                .willReturn(submission);

        // when
        SubmitResponse response = submitUseCase.execute(examId, userId, request);

        // then
        assertThat(response.status()).isEqualTo(SubmissionStatus.QUEUED);
        verify(submissionService).createAndEnqueue(examId, userId, specId, request.lang(), request.code());
    }

    @Test
    @DisplayName("제출 실패: 배정된 스펙이 없는 경우")
    void execute_Fail_NoSpec() {
        // given
        Long examId = 1L;
        Long userId = 100L;
        SubmitRequest request = new SubmitRequest("python3.11", "print('hello')");

        ExamParticipant examParticipant = ExamParticipant.builder()
                .examId(examId)
                .participantId(userId)
                .specId(null) // No spec assigned
                .build();

        given(examParticipantService.findByExamIdAndParticipantId(examId, userId)).willReturn(examParticipant);

        // when & then
        assertThatThrownBy(() -> submitUseCase.execute(examId, userId, request))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(ProblemErrorStatus.NO_ASSIGNED_PROBLEM.getCode().getCode());
    }
}

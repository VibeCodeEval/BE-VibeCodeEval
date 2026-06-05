package com.yd.vibecode.domain.submission.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.service.ParticipantSubmitOrchestrationService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;

@ExtendWith(MockitoExtension.class)
class SubmitUseCaseTest {

    @InjectMocks
    private SubmitUseCase submitUseCase;

    @Mock
    private ParticipantSubmitOrchestrationService participantSubmitOrchestrationService;

    @Test
    @DisplayName("제출 성공 — 오케스트레이션 위임")
    void execute_Success() {
        Long examId = 1L;
        Long userId = 100L;
        SubmitRequest request = new SubmitRequest("python3.11", "print('hello')");
        SubmitResponse expected = new SubmitResponse(123L, SubmissionStatus.QUEUED);

        given(participantSubmitOrchestrationService.submitOrThrow(examId, userId, request))
                .willReturn(expected);

        SubmitResponse response = submitUseCase.execute(examId, userId, request);

        assertThat(response.submissionId()).isEqualTo(123L);
        assertThat(response.status()).isEqualTo(SubmissionStatus.QUEUED);
        verify(participantSubmitOrchestrationService).submitOrThrow(examId, userId, request);
    }

    @Test
    @DisplayName("제출 실패: 이미 제출한 경우")
    void execute_Fail_AlreadySubmitted() {
        Long examId = 1L;
        Long userId = 100L;
        SubmitRequest request = new SubmitRequest("python", "print(1)");

        given(participantSubmitOrchestrationService.submitOrThrow(examId, userId, request))
                .willThrow(new RestApiException(SubmissionErrorStatus.ALREADY_SUBMITTED));

        assertThatThrownBy(() -> submitUseCase.execute(examId, userId, request))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(SubmissionErrorStatus.ALREADY_SUBMITTED.getCode().getCode());
    }
}

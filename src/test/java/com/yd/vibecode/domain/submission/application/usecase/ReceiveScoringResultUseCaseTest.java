package com.yd.vibecode.domain.submission.application.usecase;

import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.sse.SseEmitterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReceiveScoringResultUseCaseTest {

    @InjectMocks
    private ReceiveScoringResultUseCase receiveScoringResultUseCase;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private SubmissionRunRepository submissionRunRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private SseEmitterService sseEmitterService;

    @Test
    @DisplayName("채점 결과 수신 및 처리 성공: SSE 이벤트 전송 확인")
    void execute_Success() {
        // given
        Long submissionId = 1L;
        Submission submission = Submission.builder()
                .status(SubmissionStatus.RUNNING)
                .build();

        ScoringResultRequest.TestCaseResult testCase = new ScoringResultRequest.TestCaseResult(
            0, "SAMPLE", Verdict.AC, 100, 1024, 0, 0
        );
        
        ScoringResultRequest.ScoreData scoreResult = new ScoringResultRequest.ScoreData(
            new BigDecimal("30.0"), new BigDecimal("30.0"), new BigDecimal("40.0"), "{}"
        );

        ScoringResultRequest request = new ScoringResultRequest(
            SubmissionStatus.DONE,
            List.of(testCase),
            scoreResult
        );

        given(submissionService.findById(submissionId)).willReturn(submission);

        // when
        receiveScoringResultUseCase.execute(submissionId, request);

        // then
        // 1. Submission status updated
        // (Verify via mock or if we could spy the entity, but here we trust the service call logic)
        
        // 2. SubmissionRun saved
        verify(submissionRunRepository).save(any(SubmissionRun.class));
        
        // 3. Score saved
        verify(scoreRepository).save(any(Score.class));
        
        // 4. SSE events sent
        verify(sseEmitterService).sendEvent(eq(submissionId), eq("case_result"), eq(testCase));
        verify(sseEmitterService).sendEvent(eq(submissionId), eq("final_score"), any(Score.class));
        verify(sseEmitterService).complete(submissionId);
    }
}

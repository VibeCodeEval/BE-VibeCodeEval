package com.yd.vibecode.domain.submission.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.sse.SseEmitterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채점 결과 수신 및 처리 UseCase
 * - FastAPI로부터 채점 결과 수신
 * - Submission/SubmissionRun/Score 저장
 * - SSE를 통해 Admin/Master에 실시간 알림
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveScoringResultUseCase {

    private final SubmissionService submissionService;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public void execute(Long submissionId, ScoringResultRequest request) {
        // 1. Submission 상태 업데이트
        Submission submission = submissionService.findById(submissionId);
        submission.updateStatus(request.status());

        // 2. SubmissionRun 저장
        for (ScoringResultRequest.TestCaseResult testCase : request.testCases()) {
            SubmissionRun run = SubmissionRun.builder()
                    .submissionId(submissionId)
                    .caseIndex(testCase.caseIndex())
                    .grp(RunGroup.valueOf(testCase.group()))
                    .verdict(testCase.verdict())
                    .timeMs(testCase.timeMs())
                    .memKb(testCase.memKb())
                    .stdoutBytes(testCase.stdoutBytes())
                    .stderrBytes(testCase.stderrBytes())
                    .build();
            
            submissionRunRepository.save(run);
            
            // SSE: 개별 테스트 케이스 결과 전송
            sseEmitterService.sendEvent(submissionId, "case_result", testCase);
        }

        // 3. Score 저장
        if (request.score() != null) {
            Score score = Score.builder()
                    .submissionId(submissionId)
                    .promptScore(request.score().promptScore())
                    .perfScore(request.score().perfScore())
                    .correctnessScore(request.score().correctnessScore())
                    .rubricJson(request.score().rubricJson())
                    .build();
            
            score.calculateTotalScore();
            scoreRepository.save(score);
            
            // SSE: 최종 점수 전송
            sseEmitterService.sendEvent(submissionId, "final_score", score);
        }

        // 4. SSE 연결 종료
        sseEmitterService.complete(submissionId);
        
        log.info("Scoring result received and processed for submissionId={}", submissionId);
    }
}

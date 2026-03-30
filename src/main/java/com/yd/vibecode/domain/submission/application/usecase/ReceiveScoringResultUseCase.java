package com.yd.vibecode.domain.submission.application.usecase;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.domain.submission.application.event.ScoringResultSseEvent;
import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채점 결과 수신 및 처리 UseCase
 * - FastAPI로부터 채점 결과 수신
 * - Submission/SubmissionRun/Score 저장
 * - ScoringResultSseEvent publish → @TransactionalEventListener(AFTER_COMMIT)에서 SSE 전송
 *   (트랜잭션 커밋 후 전송 보장 + 지수 백오프 재시도)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveScoringResultUseCase {

    private final SubmissionService submissionService;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(Long submissionId, ScoringResultRequest request) {
        // 1. Submission 상태 업데이트
        Submission submission = submissionService.findById(submissionId);
        submission.updateStatus(request.status());

        // 2. SubmissionRun 저장
        List<ScoringResultSseEvent.CaseResultPayload> casePayloads = request.testCases().stream()
                .map(tc -> {
                    SubmissionRun run = SubmissionRun.builder()
                            .submissionId(submissionId)
                            .caseIndex(tc.caseIndex())
                            .grp(RunGroup.valueOf(tc.group()))
                            .verdict(tc.verdict())
                            .timeMs(tc.timeMs())
                            .memKb(tc.memKb())
                            .stdoutBytes(tc.stdoutBytes())
                            .stderrBytes(tc.stderrBytes())
                            .build();
                    submissionRunRepository.save(run);

                    return new ScoringResultSseEvent.CaseResultPayload(
                            tc.caseIndex(),
                            tc.verdict(),
                            tc.timeMs() != null ? tc.timeMs() : 0,
                            tc.memKb() != null ? tc.memKb() : 0
                    );
                })
                .toList();

        // 3. Score 저장
        ScoringResultSseEvent.FinalScorePayload finalScore = null;
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

            finalScore = new ScoringResultSseEvent.FinalScorePayload(
                    score.getPromptScore(),
                    score.getPerfScore(),
                    score.getCorrectnessScore(),
                    score.getTotalScore()  // calculateTotalScore() 결과 재사용
            );
        }

        // 4. SSE 이벤트 publish (트랜잭션 커밋 후 SseScoringEventListener가 처리)
        eventPublisher.publishEvent(new ScoringResultSseEvent(
                submissionId,
                request.status(),
                casePayloads,
                finalScore
        ));

        log.info("Scoring result saved, SSE event published: submissionId={}, status={}",
                submissionId, request.status());
    }
}

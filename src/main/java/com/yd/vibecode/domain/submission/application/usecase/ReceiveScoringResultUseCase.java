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
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.admin.domain.service.AdminActivityLogService;
import com.yd.vibecode.domain.auth.domain.repository.UserRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

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
    private final ExamRepository examRepository;
    private final AdminActivityLogService adminActivityLogService;
    private final UserRepository userRepository;

    @Transactional
    public void execute(Long submissionId, ScoringResultRequest request) {
        validateTestCases(request);

        Submission submission = submissionService.findById(submissionId);
        SubmissionStatus previousStatus = submission.getStatus();
        submission.updateStatus(request.status());

        submissionRunRepository.deleteBySubmissionId(submissionId);

        int passedCount = 0;
        List<ScoringResultSseEvent.CaseResultPayload> casePayloads = request.testCases().stream()
                .map(tc -> {
                    RunGroup group = parseRunGroup(tc.group());
                    SubmissionRun run = SubmissionRun.builder()
                            .submissionId(submissionId)
                            .caseIndex(tc.caseIndex())
                            .grp(group)
                            .verdict(tc.verdict())
                            .timeMs(tc.timeMs())
                            .memKb(tc.memKb())
                            .stdoutBytes(tc.stdoutBytes())
                            .stderrBytes(tc.stderrBytes())
                            .build();
                    submissionRunRepository.save(run);

                    return new ScoringResultSseEvent.CaseResultPayload(
                            tc.caseIndex(),
                            group,
                            tc.verdict(),
                            tc.timeMs() != null ? tc.timeMs() : 0,
                            tc.memKb() != null ? tc.memKb() : 0
                    );
                })
                .toList();

        for (ScoringResultSseEvent.CaseResultPayload payload : casePayloads) {
            if (payload.verdict() == Verdict.AC) {
                passedCount++;
            }
        }

        ScoringResultSseEvent.FinalScorePayload finalScore = null;
        if (request.score() != null) {
            ScoringResultRequest.ScoreData scoreData = request.score();
            Score score = scoreRepository.findBySubmissionId(submissionId)
                    .orElseGet(() -> Score.builder()
                            .submissionId(submissionId)
                            .build());
            score.updateFrom(
                    scoreData.promptScore(),
                    scoreData.perfScore(),
                    scoreData.correctnessScore(),
                    scoreData.rubricJson());
            scoreRepository.save(score);

            finalScore = new ScoringResultSseEvent.FinalScorePayload(
                    score.getPromptScore(),
                    score.getPerfScore(),
                    score.getCorrectnessScore(),
                    score.getTotalScore());

            if (request.status() == SubmissionStatus.DONE && previousStatus != SubmissionStatus.DONE) {
                examRepository.findById(submission.getExamId()).ifPresent(exam -> {
                    String participantName = userRepository.findById(submission.getParticipantId())
                            .map(user -> user.getName())
                            .orElse(null);
                    adminActivityLogService.logEvaluationCompleted(
                            exam.getCreatedBy(),
                            submission.getExamId(),
                            submission.getParticipantId(),
                            exam.getTitle(),
                            participantName);
                });
            }
        }

        ScoringResultSseEvent.CompletionPayload completion = new ScoringResultSseEvent.CompletionPayload(
                casePayloads.size(),
                passedCount);

        eventPublisher.publishEvent(new ScoringResultSseEvent(
                submissionId,
                request.status(),
                casePayloads,
                completion,
                finalScore));

        log.info(
                "Scoring result saved, SSE event published: submissionId={}, status={}, runs={}, scoreSaved={}, sseFinalScore={}",
                submissionId,
                request.status(),
                casePayloads.size(),
                request.score() != null,
                finalScore != null);
    }

    private static void validateTestCases(ScoringResultRequest request) {
        if (request.status() == SubmissionStatus.DONE && request.testCases().isEmpty()) {
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }
    }

    private static RunGroup parseRunGroup(String group) {
        try {
            return RunGroup.valueOf(group.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }
    }
}

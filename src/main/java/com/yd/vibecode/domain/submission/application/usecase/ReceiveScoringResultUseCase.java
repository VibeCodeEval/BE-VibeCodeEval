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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채점 결과 수신 및 처리 UseCase
 * - FastAPI로부터 채점 결과 수신
 * - Submission/SubmissionRun/Score 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveScoringResultUseCase {

    private final SubmissionService submissionService;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;

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
        }
        
        log.info("Scoring result received and processed for submissionId={}", submissionId);
    }
}

package com.yd.vibecode.domain.submission.application.usecase;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.application.dto.response.AdminSubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.service.SubmissionDetailAssembler;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 제출 상세 조회 (코드 본문·rubricJson 포함).
 */
@Service
@RequiredArgsConstructor
public class GetAdminSubmissionDetailUseCase {

    private final SubmissionService submissionService;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;
    private final SubmissionDetailAssembler submissionDetailAssembler;

    @Transactional(readOnly = true)
    public AdminSubmissionDetailResponse execute(Long submissionId) {
        Submission submission = submissionService.findById(submissionId);
        List<SubmissionRun> runs = submissionRunRepository.findBySubmissionId(submissionId);
        Score score = scoreRepository.findBySubmissionId(submissionId).orElse(null);

        SubmissionDetailResponse base = submissionDetailAssembler.toResponse(submission, runs, score);
        String rubricJson = score != null ? score.getRubricJson() : null;

        List<AdminSubmissionDetailResponse.CaseRunInfo> caseRuns = runs.stream()
                .sorted(Comparator.comparing(
                        SubmissionRun::getCaseIndex,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(r -> new AdminSubmissionDetailResponse.CaseRunInfo(
                        r.getCaseIndex(),
                        r.getGrp(),
                        r.getVerdict(),
                        r.getTimeMs(),
                        r.getMemKb()))
                .toList();

        return new AdminSubmissionDetailResponse(
                submission.getId(),
                base.status(),
                base.lang(),
                submission.getCodeInline(),
                base.metrics(),
                base.tc(),
                base.score(),
                rubricJson,
                caseRuns);
    }
}

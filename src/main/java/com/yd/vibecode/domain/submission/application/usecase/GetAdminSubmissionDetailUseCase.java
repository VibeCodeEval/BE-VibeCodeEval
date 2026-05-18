package com.yd.vibecode.domain.submission.application.usecase;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.domain.submission.application.dto.response.AdminSubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.service.SubmissionDetailAssembler;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 제출 상세 조회 (코드 본문·rubricJson 포함).
 */
@Service
@RequiredArgsConstructor
public class GetAdminSubmissionDetailUseCase {

    private final AdminService adminService;
    private final SubmissionService submissionService;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;
    private final SubmissionDetailAssembler submissionDetailAssembler;

    @Transactional(readOnly = true)
    public AdminSubmissionDetailResponse execute(Long adminUserId, Long submissionId) {
        validateAdminAccess(adminUserId);
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

    /**
     * 요청자가 활성화된 ADMIN/MASTER 관리자인지 DB 기준으로 검증한다.
     * (HTTP {@code /api/admin/**} 역할 검사와 별도로 UseCase 방어층)
     */
    private void validateAdminAccess(Long adminUserId) {
        Admin admin = adminService.findById(adminUserId);
        if (!Boolean.TRUE.equals(admin.getIsActive())) {
            throw new RestApiException(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE);
        }
        if (!admin.isAdmin() && !admin.isMaster()) {
            throw new RestApiException(AuthErrorStatus.FORBIDDEN);
        }
    }
}

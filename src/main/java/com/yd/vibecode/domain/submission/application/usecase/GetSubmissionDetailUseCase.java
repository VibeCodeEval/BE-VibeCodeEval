package com.yd.vibecode.domain.submission.application.usecase;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.service.SubmissionDetailAssembler;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

/**
 * 제출 상세 조회 UseCase (일반 인증 사용자용 — 코드·루브릭 미포함).
 */
@Service
@RequiredArgsConstructor
public class GetSubmissionDetailUseCase {

    private final SubmissionService submissionService;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;
    private final SubmissionDetailAssembler submissionDetailAssembler;

    @Transactional(readOnly = true)
    public SubmissionDetailResponse execute(Long currentUserId, Long submissionId) {
        Submission submission = submissionService.findById(submissionId);
        if (!Objects.equals(submission.getParticipantId(), currentUserId)) {
            throw new RestApiException(GlobalErrorStatus._FORBIDDEN);
        }
        List<SubmissionRun> runs = submissionRunRepository.findBySubmissionId(submissionId);
        Score score = scoreRepository.findBySubmissionId(submissionId).orElse(null);
        return submissionDetailAssembler.toResponse(submission, runs, score);
    }
}

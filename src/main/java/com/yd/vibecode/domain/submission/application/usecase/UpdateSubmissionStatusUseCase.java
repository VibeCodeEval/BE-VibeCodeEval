package com.yd.vibecode.domain.submission.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 제출 상태 업데이트 UseCase
 * - AI 서버로부터 제출 평가 완료 상태 수신
 * - Submission 상태 업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSubmissionStatusUseCase {

    private final SubmissionService submissionService;

    @Transactional
    public void execute(Long submissionId, String status) {
        log.info("Updating submission status: submissionId={}, status={}", submissionId, status);
        
        Submission submission = submissionService.findById(submissionId);
        if (submission == null) {
            throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
        }

        try {
            SubmissionStatus submissionStatus = SubmissionStatus.valueOf(status.toUpperCase());
            submission.updateStatus(submissionStatus);
            log.info("Submission status updated: submissionId={}, status={}", submissionId, submissionStatus);
        } catch (IllegalArgumentException e) {
            log.error("Invalid submission status: {}", status, e);
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }
    }
}


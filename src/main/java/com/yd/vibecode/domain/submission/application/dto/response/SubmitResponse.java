package com.yd.vibecode.domain.submission.application.dto.response;

import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;

public record SubmitResponse(
    Long submissionId,
    SubmissionStatus status
) {
}

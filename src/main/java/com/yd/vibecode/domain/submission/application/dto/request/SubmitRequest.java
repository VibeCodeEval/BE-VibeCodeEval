package com.yd.vibecode.domain.submission.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitRequest(
    @NotBlank(message = "언어는 필수입니다")
    String lang,
    
    @NotBlank(message = "코드는 필수입니다")
    String code
) {
}

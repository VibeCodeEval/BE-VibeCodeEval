package com.yd.vibecode.domain.exam.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExtendExamRequest(
    @NotNull(message = "연장 시간(분)은 필수입니다")
    @Min(value = 1, message = "연장 시간은 최소 1분 이상이어야 합니다")
    Integer minutes
) {
}

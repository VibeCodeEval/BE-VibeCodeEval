package com.yd.vibecode.domain.admin.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "문제 사용 가능 여부 변경 요청")
public record UpdateProblemAvailabilityRequest(
    @Schema(description = "사용 가능 여부 (true=PUBLISHED, false=ARCHIVED)", example = "true")
    @NotNull(message = "available 값은 필수입니다.")
    Boolean available
) {}

package com.yd.vibecode.domain.admin.application.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateEntryCodeRequest(
    Boolean isActive,
    @Min(value = 1, message = "토큰 한도는 1 이상이어야 합니다.")
    Integer tokenLimit
) {
}

package com.yd.vibecode.domain.admin.application.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record AdminNumberUpdateRequest(
        @Size(max = 100, message = "라벨은 100자 이하로 입력해주세요.")
        String label,

        Boolean active,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "만료일", example = "2026-01-31T23:59:59")
        LocalDateTime expiresAt
) {
}



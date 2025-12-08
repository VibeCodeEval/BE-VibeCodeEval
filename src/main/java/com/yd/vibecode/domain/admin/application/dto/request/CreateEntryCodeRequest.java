package com.yd.vibecode.domain.admin.application.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEntryCodeRequest(
    @Size(max = 100, message = "라벨은 100자 이하로 입력해주세요.")
    String label,
    
    @NotNull(message = "시험 ID는 필수입니다.")
    Long examId,
    
    Long problemSetId,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "만료일", example = "2025-12-31T23:59:59")
    @FutureOrPresent(message = "만료일은 현재 이후여야 합니다.")
    LocalDateTime expiresAt,
    
    @Min(value = 0, message = "최대 사용 횟수는 0 이상이어야 합니다.")
    Integer maxUses
) {
}

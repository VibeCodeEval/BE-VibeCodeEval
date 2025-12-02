package com.yd.vibecode.domain.admin.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record CreateEntryCodeRequest(
    String label,
    Long examId,
    Long problemSetId,
    @Schema(example = "2025-12-31T23:59:59", type = "string")
    LocalDateTime expiresAt,
    Integer maxUses
) {
}

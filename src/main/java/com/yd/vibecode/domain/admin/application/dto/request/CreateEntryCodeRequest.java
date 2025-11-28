package com.yd.vibecode.domain.admin.application.dto.request;

import java.time.LocalDateTime;

public record CreateEntryCodeRequest(
    String label,
    Long examId,
    Long problemSetId,
    LocalDateTime expiresAt,
    Integer maxUses
) {
}

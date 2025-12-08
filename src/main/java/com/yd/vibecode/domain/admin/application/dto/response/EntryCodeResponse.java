package com.yd.vibecode.domain.admin.application.dto.response;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import java.time.LocalDateTime;

public record EntryCodeResponse(
    String code,
    Long examId,
    Long problemSetId,
    String label,
    LocalDateTime expiresAt,
    Integer maxUses,
    Integer usedCount,
    Boolean isActive
) {
    public static EntryCodeResponse from(EntryCode entryCode) {
        return new EntryCodeResponse(
            entryCode.getCode(),
            entryCode.getExamId(),
            entryCode.getProblemSetId(),
            entryCode.getLabel(),
            entryCode.getExpiresAt(),
            entryCode.getMaxUses(),
            entryCode.getUsedCount(),
            entryCode.getIsActive()
        );
    }
}

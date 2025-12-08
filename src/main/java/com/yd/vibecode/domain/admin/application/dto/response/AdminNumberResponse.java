package com.yd.vibecode.domain.admin.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import java.time.LocalDateTime;

public record AdminNumberResponse(
        String adminNumber,
        String label,
        boolean active,
        Long issuedBy,
        Long assignedAdminId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime expiresAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime usedAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {

    public static AdminNumberResponse from(AdminNumber adminNumber) {
        return new AdminNumberResponse(
                adminNumber.getAdminNumber(),
                adminNumber.getLabel(),
                Boolean.TRUE.equals(adminNumber.getIsActive()),
                adminNumber.getIssuedBy(),
                adminNumber.getAssignedAdminId(),
                adminNumber.getExpiresAt(),
                adminNumber.getUsedAt(),
                adminNumber.getCreatedAt()
        );
    }
}



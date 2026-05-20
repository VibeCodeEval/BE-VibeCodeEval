package com.yd.vibecode.domain.admin.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;

public record AdminListResponse(
    List<AdminInfo> admins
) {
    public record AdminInfo(
        Long id,
        String adminNumber,
        String displayName,
        String email,
        AdminRole role,
        Boolean is2faEnabled,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime adminNumberIssuedAt,
        LocalDateTime lastLoginAt
    ) {
        public static AdminInfo from(Admin admin, LocalDateTime adminNumberIssuedAt) {
            return new AdminInfo(
                admin.getId(),
                admin.getAdminNumber(),
                admin.resolveDisplayName(),
                admin.getEmail(),
                admin.getRole(),
                admin.getIs2faEnabled(),
                admin.getIsActive(),
                admin.getCreatedAt(),
                adminNumberIssuedAt,
                admin.getLastLoginAt()
            );
        }
    }

    public static AdminListResponse of(List<AdminInfo> adminInfos) {
        return new AdminListResponse(adminInfos);
    }
}


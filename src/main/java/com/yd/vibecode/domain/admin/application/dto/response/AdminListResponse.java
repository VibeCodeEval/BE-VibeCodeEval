package com.yd.vibecode.domain.admin.application.dto.response;

import java.util.List;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;

public record AdminListResponse(
    List<AdminInfo> admins
) {
    public record AdminInfo(
        Long id,
        String adminNumber,
        String email,
        AdminRole role,
        Boolean is2faEnabled
    ) {
        public static AdminInfo from(Admin admin) {
            return new AdminInfo(
                admin.getId(),
                admin.getAdminNumber(),
                admin.getEmail(),
                admin.getRole(),
                admin.getIs2faEnabled()
            );
        }
    }

    public static AdminListResponse from(List<Admin> admins) {
        List<AdminInfo> adminInfos = admins.stream()
            .map(AdminInfo::from)
            .toList();
        return new AdminListResponse(adminInfos);
    }
}


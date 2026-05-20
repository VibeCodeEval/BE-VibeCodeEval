package com.yd.vibecode.domain.admin.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.response.AdminListResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.repository.AdminNumberRepository;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllAdminsUseCase {

    private final AdminService adminService;
    private final AdminNumberRepository adminNumberRepository;

    @Transactional(readOnly = true)
    public AdminListResponse execute(Long requesterId) {
        // Master 권한 체크
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        List<Admin> admins = adminService.findAll().stream()
                .filter(admin -> !admin.isDeleted())
                .toList();

        List<AdminListResponse.AdminInfo> adminInfos = admins.stream()
                .map(admin -> {
                    var issuedAt = adminNumberRepository.findByAdminNumber(admin.getAdminNumber())
                            .map(AdminNumber::getCreatedAt)
                            .orElse(null);
                    return AdminListResponse.AdminInfo.from(admin, issuedAt);
                })
                .toList();

        return AdminListResponse.of(adminInfos);
    }
}


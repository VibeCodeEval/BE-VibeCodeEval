package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberUpdateRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.service.AdminNumberService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateAdminNumberUseCase {

    private final AdminService adminService;
    private final AdminNumberService adminNumberService;

    @Transactional
    public AdminNumberResponse execute(Long requesterId, String adminNumber, AdminNumberUpdateRequest request) {
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        AdminNumber updated = adminNumberService.update(adminNumber, request.label(), request.active(), request.expiresAt());
        
        // 관리자 번호가 비활성화되면 해당 관리자 번호로 생성된 Admin 계정도 비활성화
        // 단, 마스터 계정은 비활성화할 수 없음
        if (request.active() != null && !request.active() && updated.getAssignedAdminId() != null) {
            Admin assignedAdmin = adminService.findById(updated.getAssignedAdminId());
            if (assignedAdmin.isMaster()) {
                throw new RestApiException(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED);
            }
            assignedAdmin.updateActive(false);
        }
        
        return AdminNumberResponse.from(updated);
    }
}



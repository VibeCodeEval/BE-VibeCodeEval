package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberUpdateRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
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
    private final MasterActivityLogService masterActivityLogService;

    @Transactional
    public AdminNumberResponse execute(Long requesterId, String adminNumber, AdminNumberUpdateRequest request) {
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        AdminNumber adminNumberBeforeUpdate = adminNumberService.getByAdminNumber(adminNumber);
        Long assignedAdminIdBeforeUpdate = adminNumberBeforeUpdate.getAssignedAdminId();
        boolean adminNumberWasActive = resolveActiveState(adminNumberBeforeUpdate.getIsActive());

        AdminNumber updatedAdminNumber = adminNumberService.update(
                adminNumber, request.label(), request.active(), request.expiresAt());

        if (request.active() != null) {
            if (assignedAdminIdBeforeUpdate != null) {
                syncAssignedAdminAndLog(
                        requesterId,
                        assignedAdminIdBeforeUpdate,
                        request.active());
            } else {
                boolean isActive = resolveActiveState(updatedAdminNumber.getIsActive());
                recordActiveStateChange(requesterId, null, null, adminNumberWasActive, isActive);
            }
        }

        return AdminNumberResponse.from(updatedAdminNumber);
    }

    private void syncAssignedAdminAndLog(Long requesterId, Long assignedAdminId, boolean requestedActive) {
        Admin assignedAdmin = adminService.findById(assignedAdminId);

        if (!requestedActive && assignedAdmin.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED);
        }

        boolean wasActive = resolveActiveState(assignedAdmin.getIsActive());
        assignedAdmin.updateActive(requestedActive);
        boolean isActive = resolveActiveState(requestedActive);

        recordActiveStateChange(
                requesterId,
                assignedAdminId,
                assignedAdmin.getDisplayName(),
                wasActive,
                isActive);
    }

    private void recordActiveStateChange(
            Long requesterId,
            Long assignedAdminId,
            String displayName,
            boolean wasActive,
            boolean isActive) {
        if (wasActive && !isActive) {
            masterActivityLogService.logSignupCodeDeactivated(requesterId, assignedAdminId, displayName);
        } else if (!wasActive && isActive) {
            masterActivityLogService.logSignupCodeReactivated(requesterId, assignedAdminId, displayName);
        }
    }

    /**
     * 가입 번호 기본 상태는 활성화. null은 활성(true)으로 간주한다.
     */
    static boolean resolveActiveState(Boolean active) {
        return !Boolean.FALSE.equals(active);
    }
}

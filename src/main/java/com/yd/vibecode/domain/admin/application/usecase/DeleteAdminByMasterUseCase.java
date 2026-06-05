package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminAccountDeletionService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteAdminByMasterUseCase {

    private static final String AUDIT_ACTION = "DELETE_ADMIN_BY_MASTER";

    private final AdminService adminService;
    private final AdminAccountDeletionService adminAccountDeletionService;
    private final MasterActivityLogService masterActivityLogService;

    @Transactional
    public void execute(Long requesterId, String targetAdminNumber) {
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        Admin target = adminService.findByAdminNumber(targetAdminNumber);
        adminAccountDeletionService.softDelete(target, requesterId, AUDIT_ACTION);
        masterActivityLogService.logAdminAccountDeleted(
                requesterId,
                target.getId(),
                target.getDisplayName());
    }
}

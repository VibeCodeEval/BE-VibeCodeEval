package com.yd.vibecode.domain.auth.domain.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.domain.service.AdminAuditLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.repository.AdminNumberRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAccountDeletionService {

    private final RefreshTokenService refreshTokenService;
    private final AdminNumberRepository adminNumberRepository;
    private final AdminAuditLogService adminAuditLogService;

    @Transactional
    public void softDelete(Admin admin, Long actorAdminId, String auditAction) {
        if (admin.isDeleted()) {
            throw new RestApiException(GlobalErrorStatus._NOT_FOUND);
        }
        if (admin.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED);
        }

        admin.delete();
        admin.updateActive(false);
        refreshTokenService.deleteRefreshToken(admin.getId().toString());

        adminNumberRepository.findByAdminNumber(admin.getAdminNumber())
                .ifPresent(this::deactivateAdminNumber);

        if (actorAdminId != null && auditAction != null) {
            adminAuditLogService.log(actorAdminId, auditAction, Map.of(
                    "targetAdminId", admin.getId(),
                    "targetAdminNumber", admin.getAdminNumber()
            ));
        }
    }

    private void deactivateAdminNumber(AdminNumber adminNumber) {
        adminNumber.update(adminNumber.getLabel(), false, adminNumber.getExpiresAt());
    }
}

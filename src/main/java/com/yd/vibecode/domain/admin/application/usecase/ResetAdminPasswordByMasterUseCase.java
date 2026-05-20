package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.ResetAdminPasswordByMasterResponse;
import com.yd.vibecode.domain.admin.domain.service.AdminAuditLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.util.TemporaryPasswordGenerator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetAdminPasswordByMasterUseCase {

    private final AdminService adminService;
    private final AdminAuditLogService adminAuditLogService;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;

    @Transactional
    public ResetAdminPasswordByMasterResponse execute(Long requesterId, String adminNumber) {
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        Admin target = adminService.findByAdminNumber(adminNumber);
        if (target.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ACCOUNT_PASSWORD_CANNOT_BE_RESET);
        }

        String temporaryPassword = temporaryPasswordGenerator.generate();
        String newPasswordHash = adminService.encodePassword(temporaryPassword);
        target.updatePassword(newPasswordHash);

        adminAuditLogService.log(requesterId, "RESET_PASSWORD_BY_MASTER", Map.of(
            "targetAdminId", target.getId(),
            "targetAdminNumber", target.getAdminNumber()
        ));

        return new ResetAdminPasswordByMasterResponse(temporaryPassword);
    }
}

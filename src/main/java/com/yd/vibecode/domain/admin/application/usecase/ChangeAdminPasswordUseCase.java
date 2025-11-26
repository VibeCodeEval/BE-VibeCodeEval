package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.request.ChangeAdminPasswordRequest;
import com.yd.vibecode.domain.admin.domain.service.AdminAuditLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeAdminPasswordUseCase {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final AdminAuditLogService adminAuditLogService;

    @Transactional
    public void execute(Long adminId, ChangeAdminPasswordRequest request) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new RestApiException(AuthErrorStatus.LOGIN_ERROR));

        // Validate current password
        adminService.validatePassword(admin, request.currentPassword());

        // Update password
        String newPasswordHash = adminService.encodePassword(request.newPassword());
        admin.updatePassword(newPasswordHash);
        
        // Audit logging
        adminAuditLogService.log(adminId, "CHANGE_PASSWORD", Map.of(
            "adminNumber", admin.getAdminNumber()
        ));
    }
}

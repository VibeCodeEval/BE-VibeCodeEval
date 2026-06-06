package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
import com.yd.vibecode.domain.auth.application.usecase.AdminLogoutUseCase;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminAccountDeletionService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.util.CookieUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteOwnAdminAccountUseCase {

    private static final String AUDIT_ACTION = "DELETE_OWN_ACCOUNT";

    private final AdminService adminService;
    private final AdminAccountDeletionService adminAccountDeletionService;
    private final MasterActivityLogService masterActivityLogService;
    private final AdminLogoutUseCase adminLogoutUseCase;
    private final CookieUtils cookieUtils;

    @Transactional
    public void execute(Long adminId, String accessToken, HttpServletResponse httpResponse) {
        Admin admin = adminService.findById(adminId);
        Long targetAdminId = admin.getId();
        String targetDisplayName = admin.getDisplayName();
        adminAccountDeletionService.softDelete(admin, adminId, AUDIT_ACTION);
        masterActivityLogService.logAdminAccountDeleted(null, targetAdminId, targetDisplayName);

        adminLogoutUseCase.execute(accessToken);
        cookieUtils.clearAccessTokenCookie(httpResponse);
        cookieUtils.clearRefreshTokenCookie(httpResponse);
    }
}

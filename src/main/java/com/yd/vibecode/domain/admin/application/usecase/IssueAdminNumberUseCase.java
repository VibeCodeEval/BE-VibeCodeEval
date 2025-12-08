package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberIssueRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.service.AdminNumberService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueAdminNumberUseCase {

    private final AdminService adminService;
    private final AdminNumberService adminNumberService;

    @Transactional
    public AdminNumberResponse execute(Long requesterId, AdminNumberIssueRequest request) {
        Admin admin = adminService.findById(requesterId);
        if (!admin.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        AdminNumber issued = adminNumberService.issue(admin.getId(), request.label(), request.expiresAt());
        return AdminNumberResponse.from(issued);
    }
}



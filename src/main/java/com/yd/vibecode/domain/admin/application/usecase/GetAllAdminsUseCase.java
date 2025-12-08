package com.yd.vibecode.domain.admin.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.response.AdminListResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllAdminsUseCase {

    private final AdminService adminService;

    @Transactional(readOnly = true)
    public AdminListResponse execute(Long requesterId) {
        // Master 권한 체크
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        // 모든 관리자 조회
        List<Admin> admins = adminService.findAll();
        return AdminListResponse.from(admins);
    }
}


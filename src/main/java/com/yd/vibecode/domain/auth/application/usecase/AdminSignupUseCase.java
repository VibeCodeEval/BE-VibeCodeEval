package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.request.AdminSignupRequest;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.service.AdminNumberService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSignupUseCase {

    private final AdminService adminService;
    private final AdminNumberService adminNumberService;

    @Transactional
    public void execute(AdminSignupRequest request) {
        // 1. 관리자 번호 검증
        AdminNumber adminNumber = adminNumberService.validateUsable(request.adminNumber());

        // 2. 중복 검증
        if (adminService.existsByAdminNumber(request.adminNumber())) {
            throw new RestApiException(AuthErrorStatus.ALREADY_REGISTERED_ADMIN_NUMBER);
        }
        if (adminService.existsByEmail(request.email())) {
            throw new RestApiException(AuthErrorStatus.ALREADY_REGISTERED_EMAIL);
        }

        // 3. 비밀번호 암호화
        String passwordHash = adminService.encodePassword(request.password());

        // 4. 관리자 생성
        var admin = adminService.create(request.adminNumber(), request.email(), passwordHash);

        // 5. 관리자 번호 사용 처리
        adminNumberService.assign(adminNumber, admin.getId());
    }
}


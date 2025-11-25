package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.request.AdminSignupRequest;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSignupUseCase {

    private final AdminService adminService;

    @Transactional
    public void execute(AdminSignupRequest request) {
        // 1. 중복 검증
        if (adminService.existsByAdminNumber(request.adminNumber())) {
            throw new RestApiException(AuthErrorStatus.ALREADY_REGISTERED_ADMIN_NUMBER);
        }
        if (adminService.existsByEmail(request.email())) {
            throw new RestApiException(AuthErrorStatus.ALREADY_REGISTERED_EMAIL);
        }

        // 2. 비밀번호 암호화
        String passwordHash = adminService.encodePassword(request.password());

        // 3. 관리자 생성
        adminService.create(request.adminNumber(), request.email(), passwordHash);
    }
}


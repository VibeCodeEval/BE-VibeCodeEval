package com.yd.vibecode.domain.auth.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.application.dto.request.AdminLoginRequest;
import com.yd.vibecode.domain.auth.application.dto.response.AdminLoginResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminLoginUseCase {

    private final AdminService adminService;
    private final TokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public AdminLoginResponse execute(AdminLoginRequest request) {
        // 1. 관리자 조회 (관리자 번호 또는 이메일로)
        Admin admin;
        if (request.identifier().contains("@")) {
            admin = adminService.findByEmail(request.identifier());
        } else {
            admin = adminService.findByAdminNumber(request.identifier());
        }

        // 2. 비밀번호 검증
        adminService.validatePassword(admin, request.password());

        // 3. JWT 토큰 생성
        String accessToken = tokenProvider.createAccessToken(
                admin.getId().toString(), "ADMIN");

        // 4. 응답 생성
        return new AdminLoginResponse(
                accessToken,
                "ADMIN",
                new AdminLoginResponse.AdminInfo(
                        admin.getId(),
                        admin.getAdminNumber(),
                        admin.getEmail()
                )
        );
    }
}


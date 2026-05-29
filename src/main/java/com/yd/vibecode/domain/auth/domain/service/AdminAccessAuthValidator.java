package com.yd.vibecode.domain.auth.domain.service;

import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

/**
 * JWT 인증 파이프라인용 관리자 access token 활성 상태 검증.
 * PasswordEncoder 등 SecurityConfig Bean에 의존하지 않도록 분리한다.
 */
@Component
@RequiredArgsConstructor
public class AdminAccessAuthValidator {

    private final AdminRepository adminRepository;

    public void validateActiveForAuthentication(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus._NOT_FOUND));
        if (admin.isDeleted() || !Boolean.TRUE.equals(admin.getIsActive())) {
            throw new RestApiException(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE);
        }
    }
}

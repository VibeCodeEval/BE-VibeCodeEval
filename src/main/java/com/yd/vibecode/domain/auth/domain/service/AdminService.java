package com.yd.vibecode.domain.auth.domain.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByAdminNumber(String adminNumber) {
        return adminRepository.existsByAdminNumber(adminNumber);
    }

    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }

    public Admin findByAdminNumber(String adminNumber) {
        return adminRepository.findByAdminNumber(adminNumber)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.LOGIN_ERROR));
    }

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.LOGIN_ERROR));
    }

    public Admin findById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus._NOT_FOUND));
    }

    /**
     * access token 인증 시 삭제·비활성 관리자의 보호 API 접근을 차단한다.
     */
    public void validateActiveForAuthentication(Long adminId) {
        Admin admin = findById(adminId);
        if (admin.isDeleted() || !Boolean.TRUE.equals(admin.getIsActive())) {
            throw new RestApiException(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE);
        }
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void validatePassword(Admin admin, String password) {
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new RestApiException(AuthErrorStatus.LOGIN_ERROR);
        }
    }

    public Admin create(String adminNumber, String displayName, String email, String passwordHash) {
        Admin admin = Admin.builder()
                .adminNumber(adminNumber)
                .displayName(displayName != null ? displayName.trim() : null)
                .email(email)
                .passwordHash(passwordHash)
                .is2faEnabled(false)
                .isActive(true)
                .build();

        return adminRepository.save(admin);
    }

    public java.util.List<Admin> findAll() {
        return adminRepository.findAll();
    }

    /** 관리자 로그인 성공 시 최근 로그인 시각 갱신 (영속 엔티티 dirty checking) */
    public void recordLastLogin(Admin admin) {
        admin.updateLastLoginAt(LocalDateTime.now());
    }
}


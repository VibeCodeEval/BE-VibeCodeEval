package com.yd.vibecode.domain.auth.domain.service;

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

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void validatePassword(Admin admin, String password) {
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new RestApiException(AuthErrorStatus.LOGIN_ERROR);
        }
    }

    public Admin create(String adminNumber, String email, String passwordHash) {
        Admin admin = Admin.builder()
                .adminNumber(adminNumber)
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
}


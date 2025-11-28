package com.yd.vibecode.domain.auth.domain.service;

import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.repository.AdminNumberRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.util.SecureRandomGenerator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminNumberService {

    private final AdminNumberRepository adminNumberRepository;
    private final SecureRandomGenerator secureRandomGenerator;

    public AdminNumber issue(Long issuedBy, String label, LocalDateTime expiresAt) {
        String adminNumber = generateUniqueAdminNumber();
        AdminNumber entity = AdminNumber.builder()
                .adminNumber(adminNumber)
                .label(label)
                .issuedBy(issuedBy)
                .expiresAt(expiresAt)
                .isActive(true)
                .build();
        return adminNumberRepository.save(entity);
    }

    public AdminNumber update(String adminNumber, String label, Boolean active, LocalDateTime expiresAt) {
        AdminNumber target = getByAdminNumber(adminNumber);
        target.update(label, active, expiresAt);
        return target;
    }

    @Transactional(readOnly = true)
    public AdminNumber validateUsable(String adminNumber) {
        AdminNumber found = getByAdminNumber(adminNumber);
        if (!found.isUsable()) {
            throw new RestApiException(AuthErrorStatus.ADMIN_NUMBER_INACTIVE);
        }
        return found;
    }

    public void assign(AdminNumber adminNumber, Long adminId) {
        adminNumber.assign(adminId, LocalDateTime.now());
    }

    private AdminNumber getByAdminNumber(String adminNumber) {
        return adminNumberRepository.findByAdminNumber(adminNumber)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_ADMIN_NUMBER));
    }

    private String generateUniqueAdminNumber() {
        while (true) {
            String candidate = "ADM-" + secureRandomGenerator.generate();
            if (!adminNumberRepository.existsByAdminNumber(candidate)) {
                return candidate;
            }
        }
    }
}



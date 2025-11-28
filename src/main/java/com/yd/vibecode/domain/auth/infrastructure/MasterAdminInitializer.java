package com.yd.vibecode.domain.auth.infrastructure;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.repository.AdminNumberRepository;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MasterAdminInitializer implements ApplicationRunner {

    private final MasterAdminProperties properties;
    private final AdminRepository adminRepository;
    private final AdminNumberRepository adminNumberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminRepository.existsByAdminNumber(properties.adminNumber())) {
            return;
        }

        log.info("[MasterAdminInitializer] Creating master admin account...");
        Admin master = adminRepository.save(Admin.builder()
                .adminNumber(properties.adminNumber())
                .email(properties.email())
                .passwordHash(passwordEncoder.encode(properties.password()))
                .role(AdminRole.MASTER)
                .is2faEnabled(false)
                .build());

        adminNumberRepository.findByAdminNumber(properties.adminNumber())
                .ifPresentOrElse(
                        number -> {
                            if (number.getAssignedAdminId() == null) {
                                number.assign(master.getId(), LocalDateTime.now());
                            }
                        },
                        () -> adminNumberRepository.save(AdminNumber.builder()
                                .adminNumber(properties.adminNumber())
                                .label("MASTER ACCOUNT")
                                .issuedBy(master.getId())
                                .assignedAdminId(master.getId())
                                .usedAt(LocalDateTime.now())
                                .isActive(false)
                                .build())
                );
    }
}



package com.yd.vibecode.domain.auth.infrastructure;

import java.time.LocalDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.repository.AdminNumberRepository;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // EnumTypeInitializer(@Order(1)) 이후에 실행되도록 설정
public class MasterAdminInitializer implements ApplicationRunner {

    private final MasterAdminProperties properties;
    private final AdminRepository adminRepository;
    private final AdminNumberRepository adminNumberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            // 테이블이 존재하는지 확인 (Hibernate가 테이블을 생성할 때까지 대기)
            if (adminRepository.existsByAdminNumber(properties.adminNumber())) {
                log.debug("[MasterAdminInitializer] Master admin already exists, skipping initialization");
                return;
            }

            log.info("[MasterAdminInitializer] Creating master admin account...");
            Admin master = adminRepository.save(Admin.builder()
                    .adminNumber(properties.adminNumber())
                    .email(properties.email())
                    .passwordHash(passwordEncoder.encode(properties.password()))
                    .role(AdminRole.MASTER)
                    .is2faEnabled(false)
                    .isActive(true)
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
            
            log.info("[MasterAdminInitializer] Master admin account created successfully");
        } catch (DataAccessException e) {
            // 테이블이 아직 생성되지 않은 경우 (Hibernate ddl-auto가 아직 실행되지 않음)
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("does not exist")) {
                log.warn("[MasterAdminInitializer] Database tables not yet created. " +
                        "Master admin initialization will be skipped. " +
                        "Please ensure Hibernate ddl-auto is set to 'update' or 'create'. " +
                        "Error: {}", errorMessage);
            } else {
                log.error("[MasterAdminInitializer] Failed to initialize master admin: {}", 
                        errorMessage, e);
                throw e;
            }
        } catch (Exception e) {
            log.error("[MasterAdminInitializer] Unexpected error during master admin initialization: {}", 
                    e.getMessage(), e);
            // 애플리케이션 시작을 막지 않도록 예외를 다시 던지지 않음
            // 필요시 주석을 해제하여 애플리케이션 시작을 중단할 수 있음
            // throw e;
        }
    }
}



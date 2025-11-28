package com.yd.vibecode.domain.admin.domain.repository;

import com.yd.vibecode.domain.admin.domain.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    List<AdminAuditLog> findByAdminIdOrderByCreatedAtDesc(Long adminId);

    List<AdminAuditLog> findByActionOrderByCreatedAtDesc(String action);
}

package com.yd.vibecode.domain.admin.domain.service;

import com.yd.vibecode.domain.admin.domain.entity.AdminAuditLog;
import com.yd.vibecode.domain.admin.domain.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    @Transactional
    public AdminAuditLog log(Long adminId, String action, Map<String, Object> details) {
        AdminAuditLog auditLog = AdminAuditLog.create(adminId, action, details);
        return adminAuditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLog> getLogsByAdminId(Long adminId) {
        return adminAuditLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLog> getLogsByAction(String action) {
        return adminAuditLogRepository.findByActionOrderByCreatedAtDesc(action);
    }
}

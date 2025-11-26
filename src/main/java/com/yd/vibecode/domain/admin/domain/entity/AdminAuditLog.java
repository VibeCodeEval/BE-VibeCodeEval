package com.yd.vibecode.domain.admin.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "admin_id")
    private Long adminId;

    @Column(nullable = false, length = 100)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Builder
    public AdminAuditLog(Long adminId, String action, Map<String, Object> details) {
        this.adminId = adminId;
        this.action = action;
        this.details = details;
    }

    public static AdminAuditLog create(Long adminId, String action, Map<String, Object> details) {
        return AdminAuditLog.builder()
                .adminId(adminId)
                .action(action)
                .details(details)
                .build();
    }
}

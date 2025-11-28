package com.yd.vibecode.domain.auth.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_numbers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminNumber extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String adminNumber;

    @Column(length = 100)
    private String label;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Long issuedBy;

    @Column
    private Long assignedAdminId;

    @Column
    private LocalDateTime usedAt;

    @Column
    private LocalDateTime expiresAt;

    @Builder
    public AdminNumber(String adminNumber, String label, Boolean isActive, Long issuedBy,
                       Long assignedAdminId, LocalDateTime usedAt, LocalDateTime expiresAt) {
        this.adminNumber = adminNumber;
        this.label = label;
        this.isActive = isActive != null ? isActive : true;
        this.issuedBy = issuedBy;
        this.assignedAdminId = assignedAdminId;
        this.usedAt = usedAt;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable() {
        return Boolean.TRUE.equals(isActive)
                && assignedAdminId == null
                && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }

    public void assign(Long adminId, LocalDateTime usedAt) {
        this.assignedAdminId = adminId;
        this.usedAt = usedAt;
        this.isActive = false;
    }

    public void update(String label, Boolean active, LocalDateTime expiresAt) {
        if (label != null) {
            this.label = label;
        }
        if (active != null) {
            this.isActive = active;
        }
        this.expiresAt = expiresAt;
    }
}



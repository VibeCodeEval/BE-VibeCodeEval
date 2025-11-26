package com.yd.vibecode.domain.auth.domain.entity;

import java.time.LocalDateTime;

import com.yd.vibecode.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "entry_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EntryCode extends BaseEntity {

    @Id
    @Column(length = 50)
    private String code;

    @Column(nullable = false)
    private Long examId;

    @Column
    private Long problemSetId;

    @Column(nullable = false)
    private Long createdBy;

    @Column(length = 100)
    private String label;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer maxUses = 0;

    @Column(nullable = false)
    private Integer usedCount = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder
    public EntryCode(String code, Long examId, Long problemSetId, Long createdBy, String label,
                     LocalDateTime expiresAt, Integer maxUses, Integer usedCount, Boolean isActive) {
        this.code = code;
        this.examId = examId;
        this.problemSetId = problemSetId;
        this.createdBy = createdBy;
        this.label = label;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses != null ? maxUses : 0;
        this.usedCount = usedCount != null ? usedCount : 0;
        this.isActive = isActive != null ? isActive : true;
    }

    public void incrementUsedCount() {
        this.usedCount++;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isMaxUsesReached() {
        return maxUses > 0 && usedCount >= maxUses;
    }

    public boolean isValid() {
        return isActive && !isExpired() && !isMaxUsesReached();
    }
}


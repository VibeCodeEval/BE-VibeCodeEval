package com.yd.vibecode.domain.admin.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_activity_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id")
    private Long masterId;

    @Column(name = "target_admin_id")
    private Long targetAdminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MasterActivityLogType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Builder
    public MasterActivityLog(
            Long masterId,
            Long targetAdminId,
            MasterActivityLogType type,
            String title,
            String message) {
        this.masterId = masterId;
        this.targetAdminId = targetAdminId;
        this.type = type;
        this.title = title;
        this.message = message;
    }
}

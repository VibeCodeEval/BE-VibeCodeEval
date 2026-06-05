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
@Table(name = "admin_activity_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "admin_id")
    private Long adminId;

    @Column(nullable = false, name = "exam_id")
    private Long examId;

    @Column(name = "participant_id")
    private Long participantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdminActivityLogType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Builder
    public AdminActivityLog(
            Long adminId,
            Long examId,
            Long participantId,
            AdminActivityLogType type,
            String title,
            String message) {
        this.adminId = adminId;
        this.examId = examId;
        this.participantId = participantId;
        this.type = type;
        this.title = title;
        this.message = message;
    }
}

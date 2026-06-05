package com.yd.vibecode.domain.admin.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 플랫폼 전역 설정 (단일 row, id=1).
 * {@code autoDeleteExpiredData=false}이면 향후 만료 데이터 자동 삭제 스케줄러는 실행되지 않아야 한다.
 */
@Entity
@Table(name = "platform_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformSettings extends BaseEntity {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "default_token_limit", nullable = false)
    private Integer defaultTokenLimit;

    @Column(name = "log_retention_days", nullable = false)
    private Integer logRetentionDays;

    @Column(name = "submission_retention_days", nullable = false)
    private Integer submissionRetentionDays;

    /**
     * false이면 만료 데이터 자동 삭제 스케줄러(향후 구현)가 실행되지 않는 정책 값이다.
     */
    @Column(name = "auto_delete_expired_data", nullable = false)
    private Boolean autoDeleteExpiredData;

    @Builder
    public PlatformSettings(
            Long id,
            Integer defaultTokenLimit,
            Integer logRetentionDays,
            Integer submissionRetentionDays,
            Boolean autoDeleteExpiredData) {
        this.id = id != null ? id : SINGLETON_ID;
        this.defaultTokenLimit = defaultTokenLimit;
        this.logRetentionDays = logRetentionDays;
        this.submissionRetentionDays = submissionRetentionDays;
        this.autoDeleteExpiredData = autoDeleteExpiredData != null ? autoDeleteExpiredData : true;
    }

    /** 데이터 보관 정책만 갱신한다. default_token_limit(DB 컬럼)은 변경하지 않는다. */
    public void updateRetention(
            int logRetentionDays,
            int submissionRetentionDays,
            boolean autoDeleteExpiredData) {
        this.logRetentionDays = logRetentionDays;
        this.submissionRetentionDays = submissionRetentionDays;
        this.autoDeleteExpiredData = autoDeleteExpiredData;
    }
}

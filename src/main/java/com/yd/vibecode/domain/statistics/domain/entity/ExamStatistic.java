package com.yd.vibecode.domain.statistics.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.yd.vibecode.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ExamStatistic 엔티티
 * - 시험 통계 버킷 저장
 * - 관리자 대시보드용 메트릭
 */
@Entity
@Table(name = "exam_statistics",
       uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "bucket_start", "bucket_sec"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamStatistic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private LocalDateTime bucketStart;

    @Column(nullable = false)
    private Integer bucketSec;

    // 운영 메트릭
    @Column
    private Integer activeExaminees;

    @Column
    private Integer judgeQueueDepth;

    @Column(precision = 10, scale = 2)
    private BigDecimal avgWaitSec;

    // 제출 메트릭
    @Column
    private Integer totalSubmissions;

    @Column
    private Integer passedSubmissions;

    @Column(precision = 5, scale = 2)
    private BigDecimal passRate;



    // 점수 통계
    @Column(precision = 5, scale = 2)
    private BigDecimal avgTotalScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal avgPromptScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal avgPerfScore;

    // 토큰 사용량
    @Column
    private Integer totalTokensUsed;

    @Column(precision = 10, scale = 2)
    private BigDecimal avgTokensPerUser;

    @Builder
    public ExamStatistic(Long examId, LocalDateTime bucketStart, Integer bucketSec,
                        Integer activeExaminees, Integer judgeQueueDepth,
                        BigDecimal avgWaitSec, Integer totalSubmissions, Integer passedSubmissions,
                        BigDecimal passRate, BigDecimal avgTotalScore, BigDecimal avgPromptScore,
                        BigDecimal avgPerfScore, Integer totalTokensUsed, BigDecimal avgTokensPerUser) {
        this.examId = examId;
        this.bucketStart = bucketStart;
        this.bucketSec = bucketSec;
        this.activeExaminees = activeExaminees;
        this.judgeQueueDepth = judgeQueueDepth;
        this.avgWaitSec = avgWaitSec;
        this.totalSubmissions = totalSubmissions;
        this.passedSubmissions = passedSubmissions;
        this.passRate = passRate;
        this.avgTotalScore = avgTotalScore;
        this.avgPromptScore = avgPromptScore;
        this.avgPerfScore = avgPerfScore;
        this.totalTokensUsed = totalTokensUsed;
        this.avgTokensPerUser = avgTokensPerUser;
    }
}

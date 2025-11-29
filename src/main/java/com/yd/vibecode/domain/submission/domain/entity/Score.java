package com.yd.vibecode.domain.submission.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;

/**
 * Score 엔티티
 * - 제출에 대한 점수 정보
 * - submissionId가 PK이자 FK (1:1 관계)
 * - 총점 = 프롬프트(40) + 성능(30) + 정답률(30)
 */
@Entity
@Table(name = "scores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Score extends BaseEntity {

    @Id
    private Long submissionId;  // PK = FK

    @Column(precision = 5, scale = 2)
    private BigDecimal promptScore;  // 프롬프트 점수 (0~40)

    @Column(precision = 5, scale = 2)
    private BigDecimal perfScore;  // 성능 점수 (0~30)

    @Column(precision = 5, scale = 2)
    private BigDecimal correctnessScore;  // 정답률 점수 (0~30)

    @Column(precision = 5, scale = 2)
    private BigDecimal totalScore;  // 총점 (0~100)

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String rubricJson;  // 채점 근거 JSON

    @Builder
    public Score(Long submissionId, BigDecimal promptScore, BigDecimal perfScore,
                BigDecimal correctnessScore, BigDecimal totalScore, String rubricJson) {
        this.submissionId = submissionId;
        this.promptScore = promptScore != null ? promptScore : BigDecimal.ZERO;
        this.perfScore = perfScore != null ? perfScore : BigDecimal.ZERO;
        this.correctnessScore = correctnessScore != null ? correctnessScore : BigDecimal.ZERO;
        this.totalScore = totalScore != null ? totalScore : BigDecimal.ZERO;
        this.rubricJson = rubricJson;
    }

    public void calculateTotalScore() {
        this.totalScore = promptScore.add(perfScore).add(correctnessScore);
    }
}

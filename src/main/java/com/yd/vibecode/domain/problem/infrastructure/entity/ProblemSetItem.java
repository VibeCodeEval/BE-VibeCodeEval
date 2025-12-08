package com.yd.vibecode.domain.problem.infrastructure.entity;

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
 * ProblemSetItem 엔티티
 * - ProblemSet과 Problem의 N:M 중계 테이블
 * - 가중치(weight) 정보 포함
 */
@Entity
@Table(name = "problem_set_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"problem_set_id", "problem_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemSetItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long problemSetId;

    @Column(nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private Double weight = 1.0;  // 기본 가중치 1.0

    @Builder
    public ProblemSetItem(Long problemSetId, Long problemId, Double weight) {
        this.problemSetId = problemSetId;
        this.problemId = problemId;
        this.weight = weight != null ? weight : 1.0;
    }

    public void updateWeight(Double weight) {
        this.weight = weight;
    }
}

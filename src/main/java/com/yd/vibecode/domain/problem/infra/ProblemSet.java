package com.yd.vibecode.domain.problem.infra;

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

/**
 * ProblemSet 엔티티
 * - 문제 세트 (문제 묶음)
 * - 시험에서 사용할 문제들의 그룹
 */
@Entity
@Table(name = "problem_sets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemSet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Long createdBy;  // 생성한 관리자 ID

    @Builder
    public ProblemSet(String name, Long createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }
}

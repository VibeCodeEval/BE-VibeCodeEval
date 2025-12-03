package com.yd.vibecode.domain.problem.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import org.hibernate.annotations.Type;

/**
 * Problem 엔티티
 * - 문제 메타 정보 관리
 * - 실제 스펙은 ProblemSpec에서 버전 단위로 관리
 */
@Entity
@Table(name = "problems")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String tags;  // JSON 배열 형태로 태그 저장

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProblemStatus status;

    @Column
    private Long currentSpecId;  // 현재 채택된 스펙 ID

    @Builder
    public Problem(String title, Difficulty difficulty, String tags, 
                  ProblemStatus status, Long currentSpecId) {
        this.title = title;
        this.difficulty = difficulty;
        this.tags = tags;
        this.status = status != null ? status : ProblemStatus.DRAFT;
        this.currentSpecId = currentSpecId;
    }

    public void updateCurrentSpecId(Long specId) {
        this.currentSpecId = specId;
    }

    public void updateStatus(ProblemStatus status) {
        this.status = status;
    }

    public void publish() {
        this.status = ProblemStatus.PUBLISHED;
    }

    public void archive() {
        this.status = ProblemStatus.ARCHIVED;
    }
}

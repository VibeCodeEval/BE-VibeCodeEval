package com.yd.vibecode.domain.problem.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * ProblemSpec 엔티티
 * - 문제의 버전별 스펙 관리
 * - (problemId, version) 유니크 제약
 * - 게시 후 불변
 */
@Entity
@Table(name = "problem_specs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"problem_id", "version"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemSpec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long specId;

    @Column(nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private Integer version;

    @Column(columnDefinition = "TEXT")
    private String contentMd;  // 문제 본문 (Markdown)

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String checkerJson;  // 체커 설정 (JSON)

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String rubricJson;  // 루브릭 (JSON)

    @Column(columnDefinition = "TEXT")
    private String changelogMd;  // 변경 이력

    @Column
    private LocalDateTime publishedAt;

    @Builder
    public ProblemSpec(Long problemId, Integer version, String contentMd,
                      String checkerJson, String rubricJson, String changelogMd,
                      LocalDateTime publishedAt) {
        this.problemId = problemId;
        this.version = version;
        this.contentMd = contentMd;
        this.checkerJson = checkerJson;
        this.rubricJson = rubricJson;
        this.changelogMd = changelogMd;
        this.publishedAt = publishedAt;
    }

    public void publish() {
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }
}

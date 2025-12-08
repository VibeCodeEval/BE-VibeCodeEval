package com.yd.vibecode.domain.chat.domain.entity;

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

import java.time.LocalDateTime;

/**
 * PromptSession 엔티티
 * - AI 대화 세션 관리
 * - exam_participants와 1:N 관계
 */
@Entity
@Table(name = "prompt_sessions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "participant_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private Long participantId;

    @Column(nullable = false)
    private Long specId;

    @Column(nullable = false)
    private Integer totalTokens = 0;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @Builder
    public PromptSession(Long examId, Long participantId, Long specId,
                        Integer totalTokens, LocalDateTime startedAt, LocalDateTime endedAt) {
        this.examId = examId;
        this.participantId = participantId;
        this.specId = specId;
        this.totalTokens = totalTokens != null ? totalTokens : 0;
        this.startedAt = startedAt != null ? startedAt : LocalDateTime.now();
        this.endedAt = endedAt;
    }

    public void addTokens(Integer tokens) {
        this.totalTokens += tokens;
    }

    public void endSession() {
        this.endedAt = LocalDateTime.now();
    }
}

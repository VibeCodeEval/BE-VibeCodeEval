package com.yd.vibecode.domain.chat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "prompt_evaluations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"session_id", "turn", "evaluation_type"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private PromptSession session;

    /**
     * turn이 null이면 세션 전체 평가 (holistic),
     * 값이 있으면 해당 턴에 대한 평가
     */
    @Column(name = "turn")
    private Integer turn;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    private EvaluationType evaluationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum EvaluationType {
        TURN_EVAL,
        HOLISTIC_FLOW
    }

    public void update(Map<String, Object> details) {
        this.details = details;
    }

    @Builder
    public PromptEvaluation(PromptSession session, Integer turn, EvaluationType evaluationType, Map<String, Object> details) {
        this.session = session;
        this.turn = turn;
        this.evaluationType = evaluationType;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }
}

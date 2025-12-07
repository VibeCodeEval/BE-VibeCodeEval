package com.yd.vibecode.domain.chat.domain.entity;

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

/**
 * PromptMessage 엔티티
 * - 대화 메시지 저장
 * - PromptSession과 1:N 관계
 */
@Entity
@Table(name = "prompt_messages",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "turn"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    @Column(nullable = false)
    private Integer turn;

    @Column(nullable = false, length = 20)
    private String role;  // USER, AI

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column
    private Integer tokenCount;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String meta;  // 메타데이터 (모델명, 온도 등)

    @Builder
    public PromptMessage(Long sessionId, Integer turn, String role, String content,
                        Integer tokenCount, String meta) {
        this.sessionId = sessionId;
        this.turn = turn;
        this.role = role;
        this.content = content;
        this.tokenCount = tokenCount;
        this.meta = meta;
    }
}

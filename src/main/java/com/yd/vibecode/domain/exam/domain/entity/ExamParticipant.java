package com.yd.vibecode.domain.exam.domain.entity;

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

@Entity
@Table(name = "exam_participants", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "participant_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private Long participantId;

    @Column
    private Long specId;

    @Column(length = 20)
    private String state;

    @Column(nullable = false)
    private Integer tokenLimit = 20000;

    @Column(nullable = false)
    private Integer tokenUsed = 0;

    @Column
    private Integer assignedSpecVersion;

    @Column
    private Long assignedProblemId;

    @Column
    private LocalDateTime joinedAt;

    @Builder
    public ExamParticipant(Long examId, Long participantId, Long specId, String state,
                          Integer tokenLimit, Integer tokenUsed, Integer assignedSpecVersion,
                          Long assignedProblemId, LocalDateTime joinedAt) {
        this.examId = examId;
        this.participantId = participantId;
        this.specId = specId;
        this.state = state;
        this.tokenLimit = tokenLimit != null ? tokenLimit : 20000;
        this.tokenUsed = tokenUsed != null ? tokenUsed : 0;
        this.assignedSpecVersion = assignedSpecVersion;
        this.assignedProblemId = assignedProblemId;
        this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
    }

    public void updateSpecId(Long specId) {
        this.specId = specId;
    }

    public void updateState(String state) {
        this.state = state;
    }

    public void addTokenUsed(Integer tokens) {
        this.tokenUsed += tokens;
    }

    public void updateAssignedSpecVersion(Integer version) {
        this.assignedSpecVersion = version;
    }

    public void updateAssignedProblemId(Long problemId) {
        this.assignedProblemId = problemId;
    }

    public boolean isTokenLimitExceeded() {
        return tokenUsed >= tokenLimit;
    }

    public Integer getRemainingTokens() {
        return Math.max(0, tokenLimit - tokenUsed);
    }
}


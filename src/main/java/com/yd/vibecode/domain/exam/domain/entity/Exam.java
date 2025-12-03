package com.yd.vibecode.domain.exam.domain.entity;

import com.yd.vibecode.global.common.BaseEntity;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamState state;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    @Column(nullable = false)
    private Integer version = 0;

    @Column(nullable = false)
    private Long createdBy;

    @Builder
    public Exam(String title, ExamState state, LocalDateTime startsAt, 
                LocalDateTime endsAt, Integer version, Long createdBy) {
        this.title = title;
        this.state = state != null ? state : ExamState.WAITING;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.version = version != null ? version : 0;
        this.createdBy = createdBy;
    }

    // 비즈니스 로직: 시험 시작
    public void start() {
        if (this.state != ExamState.WAITING) {
            throw new RestApiException(ExamErrorStatus.INVALID_EXAM_STATE);
        }
        this.state = ExamState.RUNNING;
        this.version++;
    }

    // 비즈니스 로직: 시험 종료
    public void end() {
        if (this.state != ExamState.RUNNING) {
            throw new RestApiException(ExamErrorStatus.INVALID_EXAM_STATE);
        }
        this.state = ExamState.ENDED;
        this.version++;
    }

    // 비즈니스 로직: 시험 시간 연장
    public void extend(int minutes) {
        if (this.state != ExamState.RUNNING) {
            throw new RestApiException(ExamErrorStatus.CANNOT_EXTEND_EXAM);
        }
        this.endsAt = this.endsAt.plusMinutes(minutes);
        this.version++;
    }

    // 비즈니스 로직: 시험 진행 가능 여부
    public boolean isRunning() {
        return this.state == ExamState.RUNNING;
    }

    // 비즈니스 로직: 시험 종료 여부
    public boolean isEnded() {
        return this.state == ExamState.ENDED;
    }
}

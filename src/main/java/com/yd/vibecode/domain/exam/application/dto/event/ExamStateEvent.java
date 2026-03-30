package com.yd.vibecode.domain.exam.application.dto.event;

import java.time.LocalDateTime;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;

/**
 * WebSocket 시험 상태 변경 이벤트
 * 토픽: /topic/exam/{examId}
 * 발행 시점: 시험 시작 / 종료 / 시간 연장
 */
public record ExamStateEvent(
    Long examId,
    ExamState state,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    Integer version,
    LocalDateTime serverTime
) {
    public static ExamStateEvent from(Exam exam) {
        return new ExamStateEvent(
            exam.getId(),
            exam.getState(),
            exam.getStartsAt(),
            exam.getEndsAt(),
            exam.getVersion(),
            LocalDateTime.now()
        );
    }
}

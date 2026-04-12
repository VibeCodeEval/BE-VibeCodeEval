package com.yd.vibecode.domain.exam.application.dto.response;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;

import java.time.LocalDateTime;

public record ExamResponse(
    Long id,
    String title,
    ExamState state,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    Integer version,
    Long createdBy,
    long participantCount,
    long completedCount
) {
    public static ExamResponse from(Exam exam) {
        return from(exam, 0L, 0L);
    }

    public static ExamResponse from(Exam exam, long participantCount, long completedCount) {
        return new ExamResponse(
            exam.getId(),
            exam.getTitle(),
            exam.getState(),
            exam.getStartsAt(),
            exam.getEndsAt(),
            exam.getVersion(),
            exam.getCreatedBy(),
            participantCount,
            completedCount
        );
    }
}

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
    String creatorName,
    long participantCount,
    long completedCount
) {
    private static final String UNKNOWN_CREATOR = "알 수 없음";

    public static ExamResponse from(Exam exam) {
        return from(exam, 0L, 0L, null);
    }

    public static ExamResponse from(Exam exam, long participantCount, long completedCount) {
        return from(exam, participantCount, completedCount, null);
    }

    public static ExamResponse from(
            Exam exam,
            long participantCount,
            long completedCount,
            String creatorName
    ) {
        return new ExamResponse(
            exam.getId(),
            exam.getTitle(),
            exam.getState(),
            exam.getStartsAt(),
            exam.getEndsAt(),
            exam.getVersion(),
            exam.getCreatedBy(),
            resolveCreatorName(creatorName),
            participantCount,
            completedCount
        );
    }

    private static String resolveCreatorName(String creatorName) {
        if (creatorName == null || creatorName.isBlank()) {
            return UNKNOWN_CREATOR;
        }
        return creatorName.trim();
    }
}

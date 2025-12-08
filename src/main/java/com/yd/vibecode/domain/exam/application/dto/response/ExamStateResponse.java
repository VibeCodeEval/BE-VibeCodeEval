package com.yd.vibecode.domain.exam.application.dto.response;

import com.yd.vibecode.domain.exam.domain.entity.ExamState;

import java.time.LocalDateTime;

public record ExamStateResponse(
    Long examId,
    ExamState state,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    LocalDateTime serverTime,
    Integer version
) {
    public static ExamStateResponse from(Long examId, ExamState state, LocalDateTime startsAt, 
                                        LocalDateTime endsAt, Integer version) {
        return new ExamStateResponse(
            examId,
            state,
            startsAt,
            endsAt,
            LocalDateTime.now(),
            version
        );
    }
}

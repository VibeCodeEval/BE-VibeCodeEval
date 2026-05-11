package com.yd.vibecode.domain.exam.application.dto.response;

import java.time.LocalDateTime;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;

public record ActiveSessionResponse(
    Long examId,
    Long examParticipantId,
    Long assignedProblemId,
    Long specId,
    ExamState examState,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    LocalDateTime serverTime,
    Integer tokenLimit,
    Integer tokenUsed
) {
    public static ActiveSessionResponse from(Exam exam, ExamParticipant participant) {
        return new ActiveSessionResponse(
            exam.getId(),
            participant.getId(),
            participant.getAssignedProblemId(),
            participant.getSpecId(),
            exam.getState(),
            exam.getStartsAt(),
            exam.getEndsAt(),
            LocalDateTime.now(),
            participant.getTokenLimit(),
            participant.getTokenUsed()
        );
    }
}

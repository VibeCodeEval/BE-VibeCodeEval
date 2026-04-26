package com.yd.vibecode.domain.exam.application.dto.response;

import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;

public record ParticipantSessionResponse(
    Long examParticipantId,
    Long examId,
    Integer tokenLimit,
    Integer tokenUsed,
    Long specId,
    Long assignedProblemId
) {
    public static ParticipantSessionResponse from(ExamParticipant participant) {
        return new ParticipantSessionResponse(
            participant.getId(),
            participant.getExamId(),
            participant.getTokenLimit(),
            participant.getTokenUsed(),
            participant.getSpecId(),
            participant.getAssignedProblemId()
        );
    }
}

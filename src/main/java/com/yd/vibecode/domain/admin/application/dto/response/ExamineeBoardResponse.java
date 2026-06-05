package com.yd.vibecode.domain.admin.application.dto.response;

import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExamineeBoardResponse(
    Long examParticipantId,
    String name,
    String phoneMasked,
    String state,
    Integer tokenLimit,
    Long tokenUsed,
    Boolean submitted,
    Long submissionId,
    String submissionStatus,
    BigDecimal promptScore,
    BigDecimal perfScore,
    BigDecimal correctnessScore,
    BigDecimal totalScore,
    LocalDateTime submittedAt,
    LocalDateTime evaluatedAt,
    ParticipantAttendanceStatus attendanceStatus,
    ParticipantSubmissionDisplayStatus submissionDisplayStatus
) {
    public static ExamineeBoardResponse of(
            ExamParticipant ep,
            User p,
            boolean submitted,
            Long submissionId,
            String submissionStatus,
            BigDecimal promptScore,
            BigDecimal perfScore,
            BigDecimal correctnessScore,
            BigDecimal totalScore,
            LocalDateTime submittedAt,
            LocalDateTime evaluatedAt,
            ParticipantAttendanceStatus attendanceStatus,
            ParticipantSubmissionDisplayStatus submissionDisplayStatus) {
        return new ExamineeBoardResponse(
            ep.getId(),
            p != null ? p.getName() : "",
            p != null ? maskPhone(p.getPhone()) : "",
            ep.getState(),
            ep.getTokenLimit(),
            ep.getTokenUsed().longValue(),
            submitted,
            submissionId,
            submissionStatus,
            promptScore,
            perfScore,
            correctnessScore,
            totalScore,
            submittedAt,
            evaluatedAt,
            attendanceStatus,
            submissionDisplayStatus
        );
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 9) return phone;
        String[] parts = phone.split("-");
        if (parts.length == 3) {
            return parts[0] + "-****-" + parts[2];
        }
        return phone;
    }
}

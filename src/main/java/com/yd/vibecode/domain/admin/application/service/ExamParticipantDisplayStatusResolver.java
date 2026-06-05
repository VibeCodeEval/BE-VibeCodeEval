package com.yd.vibecode.domain.admin.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.yd.vibecode.domain.admin.application.dto.response.ParticipantAttendanceStatus;
import com.yd.vibecode.domain.admin.application.dto.response.ParticipantSubmissionDisplayStatus;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;

/**
 * 시험/참가자/제출 정보로 관리자 화면용 응시·제출 표시 상태를 계산한다.
 */
public final class ExamParticipantDisplayStatusResolver {

    private static final java.util.Set<String> WAITING_STATES = java.util.Set.of(
            "WAITING", "PENDING", "IDLE");

    private ExamParticipantDisplayStatusResolver() {}

    public static boolean isExamSessionEnded(Exam exam, LocalDateTime now) {
        if (exam.getState() == ExamState.ENDED) {
            return true;
        }
        return exam.getEndsAt() != null && !exam.getEndsAt().isAfter(now);
    }

    public static ExamState displayExamState(Exam exam, LocalDateTime now) {
        if (isExamSessionEnded(exam, now) && exam.getState() != ExamState.ENDED) {
            return ExamState.ENDED;
        }
        return exam.getState();
    }

    public static ParticipantSubmissionDisplayStatus resolveSubmissionDisplayStatus(
            boolean submitted,
            Submission submission,
            Score score) {
        if (!submitted || submission == null) {
            return ParticipantSubmissionDisplayStatus.NOT_SUBMITTED;
        }
        if (isGradingComplete(submission, score)) {
            return ParticipantSubmissionDisplayStatus.GRADED;
        }
        return ParticipantSubmissionDisplayStatus.GRADING;
    }

    public static ParticipantAttendanceStatus resolveAttendanceStatus(
            Exam exam,
            ExamParticipant participant,
            boolean submitted,
            Submission submission,
            Score score,
            LocalDateTime now) {
        boolean sessionEnded = isExamSessionEnded(exam, now);

        if (sessionEnded) {
            return ParticipantAttendanceStatus.ENDED;
        }

        if (submitted && resolveSubmissionDisplayStatus(submitted, submission, score)
                != ParticipantSubmissionDisplayStatus.NOT_SUBMITTED) {
            return ParticipantAttendanceStatus.SUBMITTED;
        }

        String state = participant.getState() != null ? participant.getState().trim().toUpperCase() : "";
        if (WAITING_STATES.contains(state)) {
            return ParticipantAttendanceStatus.WAITING;
        }
        if ((participant.getTokenUsed() != null && participant.getTokenUsed() > 0)
                || participant.getJoinedAt() != null) {
            return ParticipantAttendanceStatus.IN_EXAM;
        }
        if (!state.isEmpty() && !"ENDED".equals(state)) {
            return ParticipantAttendanceStatus.IN_EXAM;
        }

        return ParticipantAttendanceStatus.WAITING;
    }

    private static boolean isGradingComplete(Submission submission, Score score) {
        if (score != null) {
            if (score.getTotalScore() != null) {
                return true;
            }
            if (score.getUpdatedAt() != null || score.getCreatedAt() != null) {
                return true;
            }
        }
        SubmissionStatus status = submission.getStatus();
        return status == SubmissionStatus.DONE || status == SubmissionStatus.FAILED;
    }

    public static boolean hasScore(Score score) {
        if (score == null) {
            return false;
        }
        BigDecimal total = score.getTotalScore();
        return total != null;
    }
}

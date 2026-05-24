package com.yd.vibecode.domain.admin.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.yd.vibecode.domain.admin.application.dto.response.ParticipantAttendanceStatus;
import com.yd.vibecode.domain.admin.application.dto.response.ParticipantSubmissionDisplayStatus;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;

class ExamParticipantDisplayStatusResolverTest {

    @Test
    @DisplayName("endsAt 경과 시 displayExamState는 ENDED")
    void displayExamState_whenEndsAtPassed() {
        Exam exam = Exam.builder()
                .title("t")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now().minusHours(2))
                .endsAt(LocalDateTime.now().minusMinutes(1))
                .version(1)
                .createdBy(1L)
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.displayExamState(exam, LocalDateTime.now()))
                .isEqualTo(ExamState.ENDED);
    }

    @Test
    @DisplayName("submission 없음 → 미제출")
    void submissionDisplay_notSubmitted() {
        assertThat(ExamParticipantDisplayStatusResolver.resolveSubmissionDisplayStatus(false, null, null))
                .isEqualTo(ParticipantSubmissionDisplayStatus.NOT_SUBMITTED);
    }

    @Test
    @DisplayName("submission 있음 + score 없음 → 채점중")
    void submissionDisplay_grading() {
        Submission sub = Submission.builder()
                .examId(1L)
                .participantId(1L)
                .specId(1L)
                .status(SubmissionStatus.QUEUED)
                .lang("python")
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.resolveSubmissionDisplayStatus(true, sub, null))
                .isEqualTo(ParticipantSubmissionDisplayStatus.GRADING);
    }

    @Test
    @DisplayName("시험 종료 후 응시상태 종료됨")
    void attendance_endedWhenSessionEnded() {
        Exam exam = Exam.builder()
                .title("t")
                .state(ExamState.ENDED)
                .startsAt(LocalDateTime.now().minusHours(2))
                .endsAt(LocalDateTime.now().minusHours(1))
                .version(2)
                .createdBy(1L)
                .build();
        ExamParticipant ep = ExamParticipant.builder()
                .examId(1L)
                .participantId(1L)
                .state("RUNNING")
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.resolveAttendanceStatus(
                exam, ep, false, null, null, LocalDateTime.now()))
                .isEqualTo(ParticipantAttendanceStatus.ENDED);
    }
}

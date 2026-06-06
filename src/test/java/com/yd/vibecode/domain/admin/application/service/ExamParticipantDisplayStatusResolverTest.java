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

    @Test
    @DisplayName("WAITING 시험 + joinedAt 있음 + 미제출 → 대기중")
    void attendance_waitingExamWithJoinedAt_notSubmitted() {
        Exam exam = waitingExam();
        ExamParticipant ep = ExamParticipant.builder()
                .examId(1L)
                .participantId(1L)
                .state("RUNNING")
                .joinedAt(LocalDateTime.now())
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.resolveAttendanceStatus(
                exam, ep, false, null, null, LocalDateTime.now()))
                .isEqualTo(ParticipantAttendanceStatus.WAITING);
    }

    @Test
    @DisplayName("WAITING 시험 + tokenUsed > 0 + 미제출 → 대기중")
    void attendance_waitingExamWithTokenUsed_notSubmitted() {
        Exam exam = waitingExam();
        ExamParticipant ep = ExamParticipant.builder()
                .examId(1L)
                .participantId(1L)
                .state("RUNNING")
                .tokenUsed(100)
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.resolveAttendanceStatus(
                exam, ep, false, null, null, LocalDateTime.now()))
                .isEqualTo(ParticipantAttendanceStatus.WAITING);
    }

    @Test
    @DisplayName("WAITING 시험 + 제출 데이터 있음 → 대기중 우선")
    void attendance_waitingExamWithSubmission_stillWaiting() {
        Exam exam = waitingExam();
        ExamParticipant ep = ExamParticipant.builder()
                .examId(1L)
                .participantId(1L)
                .state("RUNNING")
                .joinedAt(LocalDateTime.now())
                .build();
        Submission sub = Submission.builder()
                .examId(1L)
                .participantId(1L)
                .specId(1L)
                .status(SubmissionStatus.QUEUED)
                .lang("python")
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.resolveAttendanceStatus(
                exam, ep, true, sub, null, LocalDateTime.now()))
                .isEqualTo(ParticipantAttendanceStatus.WAITING);
    }

    @Test
    @DisplayName("RUNNING 시험 + joinedAt 있음 + 미제출 → 응시중")
    void attendance_runningExamWithJoinedAt_notSubmitted() {
        Exam exam = runningExam();
        ExamParticipant ep = ExamParticipant.builder()
                .examId(1L)
                .participantId(1L)
                .state("RUNNING")
                .joinedAt(LocalDateTime.now())
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.resolveAttendanceStatus(
                exam, ep, false, null, null, LocalDateTime.now()))
                .isEqualTo(ParticipantAttendanceStatus.IN_EXAM);
    }

    @Test
    @DisplayName("RUNNING 시험 + 제출 완료 → 응시완료")
    void attendance_runningExam_submitted() {
        Exam exam = runningExam();
        ExamParticipant ep = ExamParticipant.builder()
                .examId(1L)
                .participantId(1L)
                .state("RUNNING")
                .joinedAt(LocalDateTime.now())
                .build();
        Submission sub = Submission.builder()
                .examId(1L)
                .participantId(1L)
                .specId(1L)
                .status(SubmissionStatus.QUEUED)
                .lang("python")
                .build();

        assertThat(ExamParticipantDisplayStatusResolver.resolveAttendanceStatus(
                exam, ep, true, sub, null, LocalDateTime.now()))
                .isEqualTo(ParticipantAttendanceStatus.SUBMITTED);
    }

    private static Exam waitingExam() {
        return Exam.builder()
                .title("t")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now().plusHours(1))
                .endsAt(LocalDateTime.now().plusHours(2))
                .version(1)
                .createdBy(1L)
                .build();
    }

    private static Exam runningExam() {
        return Exam.builder()
                .title("t")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now().minusMinutes(10))
                .endsAt(LocalDateTime.now().plusHours(1))
                .version(1)
                .createdBy(1L)
                .build();
    }
}

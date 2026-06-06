package com.yd.vibecode.domain.exam.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.exam.application.usecase.EndExamUseCase;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

@ExtendWith(MockitoExtension.class)
class ExamAutoEndSchedulerTest {

    @InjectMocks
    private ExamAutoEndScheduler scheduler;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private EndExamUseCase endExamUseCase;

    @Test
    @DisplayName("대상 없으면 EndExamUseCase를 호출하지 않는다")
    void pollAndAutoEndExams_noCandidates() {
        given(examRepository.findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(eq(ExamState.RUNNING), any()))
                .willReturn(List.of());

        scheduler.pollAndAutoEndExams();

        verify(endExamUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("RUNNING·endsAt<=now 대상마다 EndExamUseCase를 호출한다")
    void pollAndAutoEndExams_endsEligibleExams() {
        Exam pastRunning = runningExam(1L, LocalDateTime.now().minusMinutes(5));
        Exam justNow = runningExam(2L, LocalDateTime.now());
        given(examRepository.findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(eq(ExamState.RUNNING), any()))
                .willReturn(List.of(pastRunning, justNow));

        scheduler.pollAndAutoEndExams();

        verify(endExamUseCase).execute(1L);
        verify(endExamUseCase).execute(2L);
    }

    @Test
    @DisplayName("조회는 RUNNING·endsAt<=now 조건으로 수행한다")
    void pollAndAutoEndExams_queriesWithRunningAndNowThreshold() {
        given(examRepository.findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(eq(ExamState.RUNNING), any()))
                .willReturn(List.of());

        scheduler.pollAndAutoEndExams();

        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(examRepository).findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(
                eq(ExamState.RUNNING),
                thresholdCaptor.capture()
        );
        LocalDateTime threshold = thresholdCaptor.getValue();
        Assertions.assertThat(threshold).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(2));
        Assertions.assertThat(threshold).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(2));
    }

    @Test
    @DisplayName("한 시험 실패 시 다른 시험 자동 종료는 계속된다")
    void pollAndAutoEndExams_continuesAfterSingleFailure() {
        Exam exam1 = runningExam(10L, LocalDateTime.now().minusMinutes(1));
        Exam exam2 = runningExam(20L, LocalDateTime.now().minusMinutes(1));
        given(examRepository.findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(eq(ExamState.RUNNING), any()))
                .willReturn(List.of(exam1, exam2));
        willThrow(new RuntimeException("db error"))
                .given(endExamUseCase).execute(10L);

        scheduler.pollAndAutoEndExams();

        verify(endExamUseCase).execute(10L);
        verify(endExamUseCase).execute(20L);
    }

    @Test
    @DisplayName("이미 종료된 시험(INVALID_EXAM_STATE)은 스킵하고 다음 시험을 처리한다")
    void pollAndAutoEndExams_skipsInvalidStateAndContinues() {
        Exam exam1 = runningExam(30L, LocalDateTime.now().minusMinutes(1));
        Exam exam2 = runningExam(40L, LocalDateTime.now().minusMinutes(1));
        given(examRepository.findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(eq(ExamState.RUNNING), any()))
                .willReturn(List.of(exam1, exam2));
        willThrow(new RestApiException(ExamErrorStatus.INVALID_EXAM_STATE))
                .given(endExamUseCase).execute(30L);

        scheduler.pollAndAutoEndExams();

        verify(endExamUseCase, times(1)).execute(30L);
        verify(endExamUseCase, times(1)).execute(40L);
    }

    @Test
    @DisplayName("RUNNING·endsAt>now 시험은 조회 결과에 없으면 EndExamUseCase를 호출하지 않는다")
    void pollAndAutoEndExams_doesNotEndFutureExams() {
        given(examRepository.findByStateAndEndsAtIsNotNullAndEndsAtLessThanEqual(eq(ExamState.RUNNING), any()))
                .willReturn(List.of());

        scheduler.pollAndAutoEndExams();

        verify(endExamUseCase, never()).execute(any());
    }

    private static Exam runningExam(Long id, LocalDateTime endsAt) {
        Exam exam = Exam.builder()
                .title("auto-end")
                .state(ExamState.RUNNING)
                .startsAt(endsAt.minusHours(2))
                .endsAt(endsAt)
                .version(1)
                .createdBy(1L)
                .build();
        ReflectionTestUtils.setField(exam, "id", id);
        return exam;
    }
}

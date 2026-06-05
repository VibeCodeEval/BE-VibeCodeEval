package com.yd.vibecode.domain.exam.infrastructure;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.exam.application.usecase.StartExamUseCase;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

@ExtendWith(MockitoExtension.class)
class ExamAutoStartSchedulerTest {

    @InjectMocks
    private ExamAutoStartScheduler scheduler;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private StartExamUseCase startExamUseCase;

    @Test
    @DisplayName("대상 없으면 StartExamUseCase를 호출하지 않는다")
    void pollAndAutoStartExams_noCandidates() {
        given(examRepository.findByStateAndStartsAtLessThanEqual(eq(ExamState.WAITING), org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());

        scheduler.pollAndAutoStartExams();

        verify(startExamUseCase, never()).execute(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("WAITING·startsAt<=now 대상마다 StartExamUseCase를 호출한다")
    void pollAndAutoStartExams_startsEligibleExams() {
        Exam pastWaiting = waitingExam(1L, LocalDateTime.now().minusMinutes(5));
        Exam justNow = waitingExam(2L, LocalDateTime.now());
        given(examRepository.findByStateAndStartsAtLessThanEqual(eq(ExamState.WAITING), org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of(pastWaiting, justNow));

        scheduler.pollAndAutoStartExams();

        verify(startExamUseCase).execute(1L);
        verify(startExamUseCase).execute(2L);
    }

    @Test
    @DisplayName("조회는 WAITING·startsAt<=now 조건으로 수행한다")
    void pollAndAutoStartExams_queriesWithWaitingAndNowThreshold() {
        given(examRepository.findByStateAndStartsAtLessThanEqual(eq(ExamState.WAITING), org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());

        scheduler.pollAndAutoStartExams();

        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(examRepository).findByStateAndStartsAtLessThanEqual(eq(ExamState.WAITING), thresholdCaptor.capture());
        LocalDateTime threshold = thresholdCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(threshold).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(2));
        org.assertj.core.api.Assertions.assertThat(threshold).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(2));
    }

    @Test
    @DisplayName("한 시험 실패 시 다른 시험 자동 시작은 계속된다")
    void pollAndAutoStartExams_continuesAfterSingleFailure() {
        Exam exam1 = waitingExam(10L, LocalDateTime.now().minusMinutes(1));
        Exam exam2 = waitingExam(20L, LocalDateTime.now().minusMinutes(1));
        given(examRepository.findByStateAndStartsAtLessThanEqual(eq(ExamState.WAITING), org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of(exam1, exam2));
        willThrow(new RuntimeException("db error"))
                .given(startExamUseCase).execute(10L);

        scheduler.pollAndAutoStartExams();

        verify(startExamUseCase).execute(10L);
        verify(startExamUseCase).execute(20L);
    }

    @Test
    @DisplayName("이미 시작된 시험(INVALID_EXAM_STATE)은 스킵하고 다음 시험을 처리한다")
    void pollAndAutoStartExams_skipsInvalidStateAndContinues() {
        Exam exam1 = waitingExam(30L, LocalDateTime.now().minusMinutes(1));
        Exam exam2 = waitingExam(40L, LocalDateTime.now().minusMinutes(1));
        given(examRepository.findByStateAndStartsAtLessThanEqual(eq(ExamState.WAITING), org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of(exam1, exam2));
        willThrow(new RestApiException(ExamErrorStatus.INVALID_EXAM_STATE))
                .given(startExamUseCase).execute(30L);

        scheduler.pollAndAutoStartExams();

        verify(startExamUseCase, times(1)).execute(30L);
        verify(startExamUseCase, times(1)).execute(40L);
    }

    private static Exam waitingExam(Long id, LocalDateTime startsAt) {
        Exam exam = Exam.builder()
                .title("auto-start")
                .state(ExamState.WAITING)
                .startsAt(startsAt)
                .endsAt(startsAt.plusHours(2))
                .version(0)
                .createdBy(1L)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(exam, "id", id);
        return exam;
    }
}

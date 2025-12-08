package com.yd.vibecode.domain.exam.domain.service;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @InjectMocks
    private ExamService examService;

    @Mock
    private ExamRepository examRepository;

    @Test
    @DisplayName("시험 조회 성공")
    void findById_Success() {
        // given
        Long examId = 1L;
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .createdBy(1L)
                .build();
        
        given(examRepository.findById(examId)).willReturn(Optional.of(exam));

        // when
        Exam result = examService.findById(examId);

        // then
        assertThat(result).isEqualTo(exam);
    }

    @Test
    @DisplayName("시험 조회 실패: 존재하지 않는 시험")
    void findById_Fail_NotFound() {
        // given
        Long examId = 1L;
        given(examRepository.findById(examId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> examService.findById(examId))
                .isInstanceOf(RestApiException.class)
                .extracting("errorCode.code").isEqualTo(ExamErrorStatus.EXAM_NOT_FOUND.getCode().getCode());
    }

    @Test
    @DisplayName("시험 시작 서비스 로직 성공")
    void startExam_Success() {
        // given
        Long examId = 1L;
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.WAITING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .createdBy(1L)
                .build();
        
        given(examRepository.findById(examId)).willReturn(Optional.of(exam));
        given(examRepository.save(any(Exam.class))).willReturn(exam);

        // when
        examService.startExam(examId);

        // then
        assertThat(exam.getState()).isEqualTo(ExamState.RUNNING);
        verify(examRepository).save(exam);
    }

    @Test
    @DisplayName("시험 종료 서비스 로직 성공")
    void endExam_Success() {
        // given
        Long examId = 1L;
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .createdBy(1L)
                .build();
        
        given(examRepository.findById(examId)).willReturn(Optional.of(exam));
        given(examRepository.save(any(Exam.class))).willReturn(exam);

        // when
        examService.endExam(examId);

        // then
        assertThat(exam.getState()).isEqualTo(ExamState.ENDED);
        verify(examRepository).save(exam);
    }

    @Test
    @DisplayName("시험 연장 서비스 로직 성공")
    void extendExam_Success() {
        // given
        Long examId = 1L;
        LocalDateTime endsAt = LocalDateTime.now().plusHours(1);
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(endsAt)
                .createdBy(1L)
                .build();
        
        given(examRepository.findById(examId)).willReturn(Optional.of(exam));
        given(examRepository.save(any(Exam.class))).willReturn(exam);

        // when
        examService.extendExam(examId, 30);

        // then
        assertThat(exam.getEndsAt()).isEqualTo(endsAt.plusMinutes(30));
        verify(examRepository).save(exam);
    }
}

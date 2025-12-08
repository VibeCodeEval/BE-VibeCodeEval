package com.yd.vibecode.domain.exam.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.exam.domain.service.ExamService;

@ExtendWith(MockitoExtension.class)
class StartExamUseCaseTest {

    @InjectMocks
    private StartExamUseCase startExamUseCase;

    @Mock
    private ExamService examService;

    @Test
    @DisplayName("시험 시작 UseCase 성공: 서비스 호출 확인")
    void execute_Success() {
        // given
        Long examId = 1L;

        // when
        startExamUseCase.execute(examId);

        // then
        verify(examService).startExam(examId);
    }
}

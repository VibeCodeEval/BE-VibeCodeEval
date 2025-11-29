package com.yd.vibecode.domain.exam.application.usecase;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.global.websocket.ExamBroadcastService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartExamUseCaseTest {

    @InjectMocks
    private StartExamUseCase startExamUseCase;

    @Mock
    private ExamService examService;

    @Mock
    private ExamBroadcastService examBroadcastService;

    @Test
    @DisplayName("시험 시작 UseCase 성공: 서비스 호출 및 브로드캐스트 확인")
    void execute_Success() {
        // given
        Long examId = 1L;
        Exam exam = Exam.builder()
                .title("Test Exam")
                .state(ExamState.RUNNING)
                .startsAt(LocalDateTime.now())
                .endsAt(LocalDateTime.now().plusHours(1))
                .createdBy(1L)
                .build();

        given(examService.findById(examId)).willReturn(exam);

        // when
        startExamUseCase.execute(examId);

        // then
        verify(examService).startExam(examId);
        verify(examBroadcastService).broadcastExamStarted(exam);
    }
}

package com.yd.vibecode.domain.exam.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.yd.vibecode.domain.exam.application.dto.event.ExamStateEvent;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.domain.admin.domain.service.AdminActivityLogService;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class EndExamUseCaseTest {

    @InjectMocks
    private EndExamUseCase endExamUseCase;

    @Mock
    private ExamService examService;

    @Mock
    private ExamParticipantService examParticipantService;

    @Mock
    private AutoSubmitParticipantsOnExamEndUseCase autoSubmitParticipantsOnExamEndUseCase;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AdminActivityLogService adminActivityLogService;

    @Test
    @DisplayName("시험 종료 UseCase 성공: 서비스 호출 및 WS 브로드캐스트 확인")
    void execute_Success() {
        // given
        Long examId = 1L;
        Exam exam = Exam.builder()
                .title("테스트 시험")
                .state(ExamState.ENDED)
                .startsAt(LocalDateTime.now().minusHours(2))
                .endsAt(LocalDateTime.now())
                .version(2)
                .createdBy(1L)
                .build();

        given(examService.endExam(examId)).willReturn(exam);

        // when
        endExamUseCase.execute(examId);

        // then
        verify(autoSubmitParticipantsOnExamEndUseCase).execute(examId);
        verify(examService).endExam(examId);
        verify(examParticipantService).endAllParticipants(examId);
        verify(adminActivityLogService).logExamEnded(1L, examId, "테스트 시험");
        verify(messagingTemplate).convertAndSend(eq("/topic/exam/" + examId), any(ExamStateEvent.class));
    }

    @Test
    @DisplayName("관리자 활동 로그 저장이 WebSocket 브로드캐스트보다 먼저 수행")
    void execute_logsBeforeWebSocketBroadcast() {
        Long examId = 1L;
        Exam exam = Exam.builder()
                .title("테스트 시험")
                .state(ExamState.ENDED)
                .startsAt(LocalDateTime.now().minusHours(2))
                .endsAt(LocalDateTime.now())
                .version(2)
                .createdBy(1L)
                .build();

        given(examService.endExam(examId)).willReturn(exam);

        endExamUseCase.execute(examId);

        InOrder inOrder = inOrder(adminActivityLogService, messagingTemplate);
        inOrder.verify(adminActivityLogService).logExamEnded(1L, examId, "테스트 시험");
        inOrder.verify(messagingTemplate).convertAndSend(eq("/topic/exam/" + examId), any(ExamStateEvent.class));
    }
}

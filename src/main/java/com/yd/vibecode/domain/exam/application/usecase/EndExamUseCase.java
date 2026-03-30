package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.event.ExamStateEvent;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 종료 UseCase
 * - 시험 상태를 RUNNING -> ENDED로 변경
 * - version 증가
 * - 모든 참가자 상태 ENDED로 변경
 * - WebSocket으로 상태 변경 브로드캐스트 (/topic/exam/{examId})
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EndExamUseCase {

    private final ExamService examService;
    private final ExamParticipantService examParticipantService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void execute(Long examId) {
        Exam exam = examService.endExam(examId);

        // 모든 참가자의 상태를 ENDED로 변경
        examParticipantService.endAllParticipants(examId);

        ExamStateEvent event = ExamStateEvent.from(exam);
        messagingTemplate.convertAndSend("/topic/exam/" + examId, event);
        log.info("Exam ended, WS broadcast sent: examId={}, version={}", examId, exam.getVersion());
    }
}

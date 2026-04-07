package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.event.ExamStateEvent;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.service.ExamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시험 시작 UseCase
 * - 시험 상태를 WAITING -> RUNNING으로 변경
 * - version 증가
 * - WebSocket으로 상태 변경 브로드캐스트 (/topic/exam/{examId})
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartExamUseCase {

    private final ExamService examService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void execute(Long examId) {
        Exam exam = examService.startExam(examId);

        ExamStateEvent event = ExamStateEvent.from(exam);
        messagingTemplate.convertAndSend("/topic/exam/" + examId, event);
        log.info("Exam started, WS broadcast sent: examId={}, version={}", examId, exam.getVersion());
    }
}

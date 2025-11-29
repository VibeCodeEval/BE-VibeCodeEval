package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.service.ExamService;

import lombok.RequiredArgsConstructor;

/**
 * 시험 시작 UseCase
 * - 시험 상태를 WAITING -> RUNNING으로 변경
 * - version 증가 (WebSocket 브로드캐스트용)
 */
@Service
@RequiredArgsConstructor
public class StartExamUseCase {

    private final ExamService examService;
    private final com.yd.vibecode.global.websocket.ExamBroadcastService examBroadcastService;

    @Transactional
    public void execute(Long examId) {
        examService.startExam(examId);
        
        // WebSocket을 통해 모든 클라이언트에 브로드캐스트
        com.yd.vibecode.domain.exam.domain.entity.Exam exam = examService.findById(examId);
        examBroadcastService.broadcastExamStarted(exam);
    }
}

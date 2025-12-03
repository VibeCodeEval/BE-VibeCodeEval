package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.global.websocket.ExamBroadcastService;

import lombok.RequiredArgsConstructor;

/**
 * 시험 시간 연장 UseCase
 * - 시험 종료 시각을 minutes만큼 연장
 * - version 증가
 */
@Service
@RequiredArgsConstructor
public class ExtendExamUseCase {

    private final ExamService examService;
    private final ExamBroadcastService examBroadcastService;

    @Transactional
    public void execute(Long examId, int minutes) {
        examService.extendExam(examId, minutes);
        
        // WebSocket을 통해 모든 클라이언트에 브로드캐스트
        Exam exam = examService.findById(examId);
        examBroadcastService.broadcastExamExtended(exam, minutes);
    }
}

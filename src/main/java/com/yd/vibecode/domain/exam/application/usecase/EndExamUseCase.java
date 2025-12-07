package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;

import lombok.RequiredArgsConstructor;

/**
 * 시험 종료 UseCase
 * - 시험 상태를 RUNNING -> ENDED로 변경
 * - version 증가
 */
@Service
@RequiredArgsConstructor
public class EndExamUseCase {

    private final ExamService examService;
    private final ExamParticipantService examParticipantService;

    @Transactional
    public void execute(Long examId) {
        examService.endExam(examId);
        
        // 모든 참가자의 상태를 ENDED로 변경
        examParticipantService.endAllParticipants(examId);
    }
}

package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.service.ExamService;

import lombok.RequiredArgsConstructor;

/**
 * 시험 시작 UseCase
 * - 시험 상태를 WAITING -> RUNNING으로 변경
 * - version 증가
 */
@Service
@RequiredArgsConstructor
public class StartExamUseCase {

    private final ExamService examService;

    @Transactional
    public void execute(Long examId) {
        examService.startExam(examId);
    }
}

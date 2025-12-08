package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.service.ExamService;

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

    @Transactional
    public void execute(Long examId, int minutes) {
        examService.extendExam(examId, minutes);
    }
}

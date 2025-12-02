package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteExamUseCase {

    private final ExamRepository examRepository;

    @Transactional
    public void execute(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RestApiException(ExamErrorStatus.EXAM_NOT_FOUND));
        
        examRepository.delete(exam);
    }
}

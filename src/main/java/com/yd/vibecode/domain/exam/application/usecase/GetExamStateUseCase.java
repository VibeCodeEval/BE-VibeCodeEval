package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.response.ExamStateResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.service.ExamService;

import lombok.RequiredArgsConstructor;

/**
 * 시험 상태 조회 UseCase (USER)
 * - 클라이언트 타이머 동기화용
 * - 서버 시각 기준으로 응답
 */
@Service
@RequiredArgsConstructor
public class GetExamStateUseCase {

    private final ExamService examService;

    @Transactional(readOnly = true)
    public ExamStateResponse execute(Long examId) {
        Exam exam = examService.findById(examId);
        
        return ExamStateResponse.from(
            exam.getId(),
            exam.getState(),
            exam.getStartsAt(),
            exam.getEndsAt(),
            exam.getVersion()
        );
    }
}

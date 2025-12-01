package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.request.CreateExamRequest;
import com.yd.vibecode.domain.exam.application.dto.response.ExamResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateExamUseCase {

    private final ExamRepository examRepository;

    @Transactional
    public ExamResponse execute(Long adminId, CreateExamRequest request) {
        Exam exam = Exam.builder()
            .title(request.title())
            .state(ExamState.WAITING)
            .startsAt(request.startsAt())
            .endsAt(request.endsAt())
            .version(0)
            .createdBy(adminId)
            .build();

        Exam saved = examRepository.save(exam);
        
        return ExamResponse.from(saved);
    }
}

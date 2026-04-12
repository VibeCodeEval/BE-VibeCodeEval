package com.yd.vibecode.domain.admin.application.usecase;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.response.ExamResponse;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.repository.ExamParticipantRepository;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExamsUseCase {

    private final ExamRepository examRepository;
    private final ExamParticipantRepository examParticipantRepository;
    private final SubmissionRepository submissionRepository;

    public List<ExamResponse> execute() {
        List<Exam> exams = examRepository.findAll();
        
        return exams.stream()
            .map(exam -> ExamResponse.from(
                exam,
                examParticipantRepository.countByExamId(exam.getId()),
                submissionRepository.countByExamId(exam.getId())
            ))
            .collect(Collectors.toList());
    }
}

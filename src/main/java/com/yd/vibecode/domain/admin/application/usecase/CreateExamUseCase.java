package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.request.CreateExamRequest;
import com.yd.vibecode.domain.exam.application.dto.response.ExamResponse;
import java.util.List;
import java.util.Random;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.domain.exam.domain.entity.Exam;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.domain.exam.domain.repository.ExamRepository;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.domain.problem.infra.ProblemSet;
import com.yd.vibecode.domain.problem.infra.ProblemSetItem;
import com.yd.vibecode.domain.problem.infra.ProblemSetItemRepository;
import com.yd.vibecode.domain.problem.infra.ProblemSetRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import com.yd.vibecode.global.util.CodeGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateExamUseCase {

    private final ExamRepository examRepository;
    private final ProblemRepository problemRepository;
    private final ProblemSetRepository problemSetRepository;
    private final ProblemSetItemRepository problemSetItemRepository;
    private final EntryCodeRepository entryCodeRepository;

    @Transactional
    public ExamResponse execute(Long adminId, CreateExamRequest request) {
        // 1. 시험 생성
        Exam exam = Exam.builder()
            .title(request.title())
            .state(ExamState.WAITING)
            .startsAt(request.startsAt())
            .endsAt(request.endsAt())
            .version(0)
            .createdBy(adminId)
            .build();

        Exam savedExam = examRepository.save(exam);

        // 2. PUBLISHED 상태의 문제 중 랜덤 선택
        List<Problem> publishedProblems = problemRepository.findByStatus(ProblemStatus.PUBLISHED);
        if (publishedProblems.isEmpty()) {
            throw new RestApiException(ProblemErrorStatus.NO_PUBLISHED_PROBLEMS);
        }
        
        Problem randomProblem = publishedProblems.get(new Random().nextInt(publishedProblems.size()));

        // 3. 문제 세트 생성
        ProblemSet problemSet = ProblemSet.builder()
            .name(savedExam.getTitle() + " 문제 세트")
            .createdBy(adminId)
            .build();
        
        ProblemSet savedProblemSet = problemSetRepository.save(problemSet);

        // 4. 문제 세트 아이템 생성 (문제 연결)
        ProblemSetItem problemSetItem = ProblemSetItem.builder()
            .problemSetId(savedProblemSet.getId())
            .problemId(randomProblem.getId())
            .weight(1.0)
            .build();
        
        problemSetItemRepository.save(problemSetItem);

        // 5. 입장 코드 생성
        String code = CodeGenerator.generate();
        EntryCode entryCode = EntryCode.builder()
            .code(code)
            .examId(savedExam.getId())
            .problemSetId(savedProblemSet.getId())
            .createdBy(adminId)
            .label(savedExam.getTitle())
            .expiresAt(savedExam.getEndsAt())
            .maxUses(100) // 기본값 100
            .isActive(true)
            .build();
        
        entryCodeRepository.save(entryCode);
        
        return ExamResponse.from(savedExam);
    }
}

package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.request.CreateProblemRequest;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.service.ProblemService;

import lombok.RequiredArgsConstructor;

/**
 * 문제 생성 UseCase (ADMIN-009)
 * - 새로운 문제를 DRAFT 상태로 생성
 */
@Service
@RequiredArgsConstructor
public class CreateProblemUseCase {

    private final ProblemService problemService;

    @Transactional
    public ProblemResponse execute(CreateProblemRequest request) {
        Problem problem = Problem.builder()
                .title(request.title())
                .difficulty(request.difficulty())
                .tags(request.tags())
                .build();

        Problem saved = problemService.create(problem);
        return ProblemResponse.from(saved);
    }
}

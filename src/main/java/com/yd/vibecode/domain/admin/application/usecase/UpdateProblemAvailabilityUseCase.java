package com.yd.vibecode.domain.admin.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateProblemAvailabilityRequest;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProblemAvailabilityUseCase {

    private final ProblemRepository problemRepository;

    @Transactional
    public ProblemResponse execute(Long problemId, UpdateProblemAvailabilityRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RestApiException(ProblemErrorStatus.PROBLEM_NOT_FOUND));

        if (Boolean.TRUE.equals(request.available())) {
            if (problem.getStatus() != ProblemStatus.PUBLISHED) {
                problem.publish();
            }
        } else {
            if (problem.getStatus() == ProblemStatus.PUBLISHED) {
                List<Problem> publishedProblems =
                        problemRepository.findAllByStatusForUpdate(ProblemStatus.PUBLISHED);
                if (publishedProblems.size() <= 1) {
                    throw new RestApiException(ProblemErrorStatus.LAST_AVAILABLE_PROBLEM_REQUIRED);
                }
                Problem publishedTarget = publishedProblems.stream()
                        .filter(p -> p.getId().equals(problemId))
                        .findFirst()
                        .orElseThrow(() -> new RestApiException(ProblemErrorStatus.PROBLEM_NOT_FOUND));
                publishedTarget.archive();
                return ProblemResponse.from(publishedTarget);
            } else if (problem.getStatus() != ProblemStatus.ARCHIVED) {
                problem.archive();
            }
        }

        return ProblemResponse.from(problem);
    }
}

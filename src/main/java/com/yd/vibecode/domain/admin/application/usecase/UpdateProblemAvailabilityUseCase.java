package com.yd.vibecode.domain.admin.application.usecase;

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
                if (problemRepository.countByStatus(ProblemStatus.PUBLISHED) <= 1) {
                    throw new RestApiException(ProblemErrorStatus.LAST_AVAILABLE_PROBLEM_REQUIRED);
                }
                problem.archive();
            } else if (problem.getStatus() != ProblemStatus.ARCHIVED) {
                problem.archive();
            }
        }

        return ProblemResponse.from(problem);
    }
}

package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteProblemUseCase {

    private final ProblemRepository problemRepository;

    @Transactional
    public void execute(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new RestApiException(ProblemErrorStatus.PROBLEM_NOT_FOUND));
        
        problemRepository.delete(problem);
    }
}

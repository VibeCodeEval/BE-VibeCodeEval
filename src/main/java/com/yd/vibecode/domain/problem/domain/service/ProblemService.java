package com.yd.vibecode.domain.problem.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    @Transactional(readOnly = true)
    public Problem findById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ProblemErrorStatus.PROBLEM_NOT_FOUND));
    }

    public Problem create(Problem problem) {
        return problemRepository.save(problem);
    }
}

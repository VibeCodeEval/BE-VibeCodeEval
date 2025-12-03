package com.yd.vibecode.domain.problem.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;
import com.yd.vibecode.domain.problem.domain.repository.ProblemSpecRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProblemSpecService {

    private final ProblemSpecRepository problemSpecRepository;

    @Transactional(readOnly = true)
    public ProblemSpec findBySpecId(Long specId) {
        return problemSpecRepository.findBySpecId(specId)
                .orElseThrow(() -> new RestApiException(ProblemErrorStatus.SPEC_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ProblemSpec findByProblemIdAndVersion(Long problemId, Integer version) {
        return problemSpecRepository.findByProblemIdAndVersion(problemId, version)
                .orElseThrow(() -> new RestApiException(ProblemErrorStatus.SPEC_NOT_FOUND));
    }

    public ProblemSpec create(ProblemSpec spec) {
        return problemSpecRepository.save(spec);
    }
}

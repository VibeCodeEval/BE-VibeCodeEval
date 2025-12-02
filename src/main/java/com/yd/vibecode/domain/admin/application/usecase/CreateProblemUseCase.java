package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.request.CreateProblemRequest;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProblemUseCase {

    private final ProblemRepository problemRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProblemResponse execute(CreateProblemRequest request) {
        String tagsJson = "[]";
        if (request.tags() != null && !request.tags().isEmpty()) {
            try {
                tagsJson = objectMapper.writeValueAsString(request.tags());
            } catch (JsonProcessingException e) {
                throw new RestApiException(GlobalErrorStatus._INTERNAL_SERVER_ERROR);
            }
        }

        Problem problem = Problem.builder()
            .title(request.title())
            .difficulty(request.difficulty())
            .tags(tagsJson)
            .status(ProblemStatus.DRAFT)
            .build();

        Problem savedProblem = problemRepository.save(problem);
        return ProblemResponse.from(savedProblem);
    }
}

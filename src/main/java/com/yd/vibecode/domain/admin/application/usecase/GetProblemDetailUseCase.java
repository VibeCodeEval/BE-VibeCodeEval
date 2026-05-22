package com.yd.vibecode.domain.admin.application.usecase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemDetailResponse;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.repository.ProblemSpecRepository;
import com.yd.vibecode.domain.problem.domain.service.ProblemService;
import com.yd.vibecode.domain.problem.domain.service.ProblemSpecService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자/마스터 — problemId 기준 문제 상세(공개 스펙) 조회
 */
@Service
@RequiredArgsConstructor
public class GetProblemDetailUseCase {

    private final ProblemService problemService;
    private final ProblemSpecService problemSpecService;
    private final ProblemSpecRepository problemSpecRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ProblemDetailResponse execute(Long problemId) {
        Problem problem = problemService.findById(problemId);
        ProblemSpec spec = resolveCurrentSpec(problem);
        return buildResponse(problem, spec);
    }

    private ProblemSpec resolveCurrentSpec(Problem problem) {
        if (problem.getCurrentSpecId() != null) {
            return problemSpecService.findBySpecId(problem.getCurrentSpecId());
        }
        return problemSpecRepository.findByProblemIdOrderByVersionDesc(problem.getId()).stream()
            .findFirst()
            .orElseThrow(() -> new RestApiException(ProblemErrorStatus.SPEC_NOT_FOUND));
    }

    private ProblemDetailResponse buildResponse(Problem problem, ProblemSpec spec) {
        try {
            List<String> tags = objectMapper.readValue(
                problem.getTags() != null ? problem.getTags() : "[]",
                new TypeReference<List<String>>() {}
            );

            Map<String, Object> checkerMap = objectMapper.readValue(
                spec.getCheckerJson() != null ? spec.getCheckerJson() : "{}",
                new TypeReference<Map<String, Object>>() {}
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> limitsMap = (Map<String, Object>) checkerMap.getOrDefault("limits", Map.of());
            int timeMs = limitsMap.containsKey("timeMs")
                ? ((Number) limitsMap.get("timeMs")).intValue()
                : 2000;
            int memoryMb = limitsMap.containsKey("memoryMb")
                ? ((Number) limitsMap.get("memoryMb")).intValue()
                : 512;

            @SuppressWarnings("unchecked")
            Map<String, Object> restrictionsMap = (Map<String, Object>) checkerMap.getOrDefault("restrictions", Map.of());
            @SuppressWarnings("unchecked")
            List<String> allowedLangs = (List<String>) restrictionsMap.getOrDefault(
                "allowedLangs",
                List.of("cpp17", "python3.11")
            );
            @SuppressWarnings("unchecked")
            List<String> forbiddenApis = (List<String>) restrictionsMap.getOrDefault("forbiddenApis", List.of());

            String checkerType = checkerMap.getOrDefault("type", "equality").toString();
            ProblemStatus status = problem.getStatus();
            boolean usable = status == ProblemStatus.PUBLISHED;

            return new ProblemDetailResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDifficulty(),
                tags,
                spec.getVersion(),
                spec.getContentMd() != null ? spec.getContentMd() : "",
                new ProblemDetailResponse.LimitsInfo(timeMs, memoryMb),
                new ProblemDetailResponse.RestrictionsInfo(allowedLangs, forbiddenApis),
                new ProblemDetailResponse.CheckerInfo(checkerType),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),
                spec.getPublishedAt(),
                status,
                usable
            );
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build problem detail response", e);
        }
    }
}

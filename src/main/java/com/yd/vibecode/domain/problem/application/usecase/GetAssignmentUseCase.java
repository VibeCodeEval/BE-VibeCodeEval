package com.yd.vibecode.domain.problem.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.problem.application.dto.response.AssignmentResponse;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemSpec;
import com.yd.vibecode.domain.problem.domain.service.ProblemService;
import com.yd.vibecode.domain.problem.domain.service.ProblemSpecService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ProblemErrorStatus;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 배정 문제 조회 UseCase
 * - ExamParticipant의 assignedProblemId와 specId를 통해 문제 및 스펙 조회
 * - 세션 시작 시 spec_id 잠금 적용됨
 */
@Service
@RequiredArgsConstructor
public class GetAssignmentUseCase {

    private final ExamParticipantService examParticipantService;
    private final ProblemService problemService;
    private final ProblemSpecService problemSpecService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AssignmentResponse execute(Long examId, Long userId) {
        // 1. ExamParticipant 조회
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(examId, userId);
        
        if (examParticipant == null || examParticipant.getAssignedProblemId() == null) {
            throw new RestApiException(ProblemErrorStatus.NO_ASSIGNED_PROBLEM);
        }

        // 2. Problem 조회
        Problem problem = problemService.findById(examParticipant.getAssignedProblemId());

        // 3. ProblemSpec 조회 (specId로 조회)
        ProblemSpec spec = problemSpecService.findBySpecId(examParticipant.getSpecId());

        // 4. 응답 구성
        return buildResponse(problem, spec);
    }

    private AssignmentResponse buildResponse(Problem problem, ProblemSpec spec) {
        try {
            // JSON 파싱
            List<String> tags = objectMapper.readValue(
                problem.getTags() != null ? problem.getTags() : "[]",
                new TypeReference<List<String>>() {}
            );

            Map<String, Object> checkerMap = objectMapper.readValue(
                spec.getCheckerJson() != null ? spec.getCheckerJson() : "{}",
                new TypeReference<Map<String, Object>>() {}
            );

            // ProblemInfo 구성
            AssignmentResponse.ProblemInfo problemInfo = new AssignmentResponse.ProblemInfo(
                problem.getId(),
                problem.getTitle(),
                spec.getContentMd(),
                tags,
                problem.getDifficulty()
            );

            // Limits 추출 (기본값: 2000ms, 512MB)
            Map<String, Object> limitsMap = (Map<String, Object>) checkerMap.getOrDefault("limits", Map.of());
            int timeMs = limitsMap.containsKey("timeMs") ? 
                ((Number) limitsMap.get("timeMs")).intValue() : 2000;
            int memoryMb = limitsMap.containsKey("memoryMb") ? 
                ((Number) limitsMap.get("memoryMb")).intValue() : 512;

            // Restrictions 추출
            Map<String, Object> restrictionsMap = (Map<String, Object>) checkerMap.getOrDefault("restrictions", Map.of());
            List<String> allowedLangs = (List<String>) restrictionsMap.getOrDefault("allowedLangs", 
                List.of("cpp17", "python3.11"));
            List<String> forbiddenApis = (List<String>) restrictionsMap.getOrDefault("forbiddenApis", List.of());

            // SpecInfo 구성
            AssignmentResponse.SpecInfo specInfo = new AssignmentResponse.SpecInfo(
                spec.getVersion(),
                new AssignmentResponse.LimitsInfo(timeMs, memoryMb),
                new AssignmentResponse.RestrictionsInfo(allowedLangs, forbiddenApis),
                new AssignmentResponse.CheckerInfo(
                    checkerMap.getOrDefault("type", "equality").toString()
                )
            );

            return new AssignmentResponse(problemInfo, specInfo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse problem spec", e);
        }
    }
}

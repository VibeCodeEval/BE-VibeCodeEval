package com.yd.vibecode.domain.admin.application.dto.response;

import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ProblemResponse(
    @Schema(description = "문제 ID", example = "1")
    Long id,

    @Schema(description = "문제 제목", example = "피보나치 수열 구하기")
    String title,

    @Schema(description = "난이도", example = "EASY")
    Difficulty difficulty,

    @Schema(description = "태그 목록 (JSON String)", example = "[\"DP\", \"Math\"]")
    String tags,

    @Schema(description = "상태", example = "DRAFT")
    ProblemStatus status,

    @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt
) {
    public static ProblemResponse from(Problem problem) {
        return new ProblemResponse(
            problem.getId(),
            problem.getTitle(),
            problem.getDifficulty(),
            problem.getTags(),
            problem.getStatus(),
            problem.getCreatedAt()
        );
    }
}

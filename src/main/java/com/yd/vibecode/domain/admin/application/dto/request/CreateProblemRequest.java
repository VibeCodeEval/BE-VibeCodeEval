package com.yd.vibecode.domain.admin.application.dto.request;

import com.yd.vibecode.domain.problem.domain.entity.Difficulty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "문제 생성 요청")
public record CreateProblemRequest(
    @Schema(description = "문제 제목", example = "BFS로 최단 거리 구하기")
    @NotBlank(message = "문제 제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    String title,

    @Schema(description = "난이도", example = "MEDIUM")
    @NotNull(message = "난이도는 필수입니다.")
    Difficulty difficulty,

    @Schema(description = "태그 목록 (JSON 배열 문자열)", example = "[\"BFS\", \"Graph\"]")
    @Size(max = 1000, message = "태그는 1000자 이하여야 합니다.")
    String tags
) {}

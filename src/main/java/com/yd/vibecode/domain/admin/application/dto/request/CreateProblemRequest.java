package com.yd.vibecode.domain.admin.application.dto.request;

import com.yd.vibecode.domain.problem.domain.entity.Difficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateProblemRequest(
    @Schema(description = "문제 제목", example = "피보나치 수열 구하기")
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @Schema(description = "난이도", example = "EASY")
    @NotNull(message = "난이도는 필수입니다.")
    Difficulty difficulty,

    @Schema(description = "태그 목록", example = "[\"DP\", \"Math\"]")
    List<String> tags
) {}

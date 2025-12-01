package com.yd.vibecode.domain.exam.application.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExamRequest(
    @NotBlank(message = "시험 제목은 필수입니다")
    String title,
    
    @NotNull(message = "시작 시각은 필수입니다")
    LocalDateTime startsAt,
    
    @NotNull(message = "종료 시각은 필수입니다")
    LocalDateTime endsAt
) {
}

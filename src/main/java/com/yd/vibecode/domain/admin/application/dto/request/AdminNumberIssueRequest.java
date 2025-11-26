package com.yd.vibecode.domain.admin.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AdminNumberIssueRequest(
        @Size(max = 100, message = "라벨은 100자 이하로 입력해주세요.")
        String label,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @FutureOrPresent(message = "만료일은 현재 이후여야 합니다.")
        LocalDateTime expiresAt
) {
}



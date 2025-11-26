package com.yd.vibecode.domain.admin.application.dto.response;

import java.time.LocalDateTime;

public record ProblemSpecResponse(
    Long specId,
    Integer version,
    String changelogMd,
    LocalDateTime publishedAt
) {
}

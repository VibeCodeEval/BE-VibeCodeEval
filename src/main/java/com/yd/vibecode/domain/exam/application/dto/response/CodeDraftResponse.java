package com.yd.vibecode.domain.exam.application.dto.response;

import java.time.LocalDateTime;

public record CodeDraftResponse(
        String language,
        String codeInline,
        LocalDateTime savedAt
) {}

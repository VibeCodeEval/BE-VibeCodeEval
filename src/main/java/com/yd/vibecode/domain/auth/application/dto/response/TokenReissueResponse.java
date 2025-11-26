package com.yd.vibecode.domain.auth.application.dto.response;

public record TokenReissueResponse(
        String accessToken,
        String refreshToken
) {
}

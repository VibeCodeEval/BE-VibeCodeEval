package com.yd.vibecode.domain.auth.application.dto.response;

public record AdminLoginResponse(
    String accessToken,
    String role,
    AdminInfo admin
) {
    public record AdminInfo(
        Long id,
        String adminNumber,
        String email
    ) {
    }
}

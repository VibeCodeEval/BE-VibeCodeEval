package com.yd.vibecode.domain.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
    @NotBlank(message = "관리자 번호 또는 이메일은 필수입니다.")
    String identifier,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {
}

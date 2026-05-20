package com.yd.vibecode.domain.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSignupRequest(
    @NotBlank(message = "관리자 번호는 필수입니다.")
    String adminNumber,

    @NotBlank(message = "관리자 이름은 필수입니다.")
    @Size(max = 100, message = "관리자 이름은 100자 이하여야 합니다.")
    String displayName,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {
}

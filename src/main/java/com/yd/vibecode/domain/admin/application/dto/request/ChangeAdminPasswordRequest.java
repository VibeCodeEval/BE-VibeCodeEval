package com.yd.vibecode.domain.admin.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeAdminPasswordRequest(
    @NotBlank
    String currentPassword,
    @NotBlank
    String newPassword
) {
}

package com.yd.vibecode.domain.auth.infrastructure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "master.admin")
@Validated
public record MasterAdminProperties(
        @NotBlank String adminNumber,
        @NotBlank String email,
        @NotBlank String password
) {
}



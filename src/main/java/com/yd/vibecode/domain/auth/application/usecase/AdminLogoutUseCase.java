package com.yd.vibecode.domain.auth.application.usecase;

import com.yd.vibecode.domain.auth.domain.service.RefreshTokenService;
import com.yd.vibecode.domain.auth.domain.service.TokenBlacklistService;
import com.yd.vibecode.global.security.TokenProvider;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLogoutUseCase {

    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;

    @Transactional
    public void execute(String accessToken) {
        String adminId = tokenProvider.getId(accessToken).orElse(null);
        if (adminId != null) {
            refreshTokenService.deleteRefreshToken(adminId);
        }

        Duration remaining = tokenProvider.getRemainingDuration(accessToken)
                .orElse(Duration.ZERO);

        if (remaining.isNegative()) {
            remaining = Duration.ZERO;
        }

        tokenBlacklistService.blacklist(accessToken, remaining.isZero() ? Duration.ofMinutes(1) : remaining);
    }
}



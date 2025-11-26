package com.yd.vibecode.domain.auth.domain.service;

import static com.yd.vibecode.global.exception.code.status.AuthErrorStatus.INVALID_REFRESH_TOKEN;
import static com.yd.vibecode.global.exception.code.status.AuthErrorStatus.EXPIRED_REFRESH_TOKEN;

import com.yd.vibecode.domain.auth.application.dto.response.TokenReissueResponse;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.security.TokenProvider;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenReissueService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenReissueResponse reissue(String refreshToken, String userId) {

        // 존재 유무 검사
        if (!refreshTokenService.isExist(refreshToken, userId)) {
            throw new RestApiException(INVALID_REFRESH_TOKEN);
        }

        // 기존에 있는 토큰 삭제
        refreshTokenService.deleteRefreshToken(userId);

        // 새 토큰 발급
        String newAccessToken = tokenProvider.createAccessToken(userId);
        String newRefreshToken = tokenProvider.createRefreshToken(userId);
        Duration duration = tokenProvider.getRemainingDuration(refreshToken)
                .orElseThrow(() -> new RestApiException(EXPIRED_REFRESH_TOKEN));

        // 저장
        refreshTokenService.saveRefreshToken(userId, newRefreshToken, duration);

        return new TokenReissueResponse(newAccessToken, newRefreshToken);
    }
}
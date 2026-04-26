package com.yd.vibecode.domain.auth.ui;

import java.time.Duration;

import com.yd.vibecode.domain.auth.application.dto.request.AdminLoginRequest;
import com.yd.vibecode.domain.auth.application.dto.request.AdminSignupRequest;
import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.application.dto.response.AdminLoginResponse;
import com.yd.vibecode.domain.auth.application.dto.response.EnterResponse;
import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.domain.auth.application.usecase.AdminLoginUseCase;
import com.yd.vibecode.domain.auth.application.usecase.AdminLogoutUseCase;
import com.yd.vibecode.domain.auth.application.usecase.AdminSignupUseCase;
import com.yd.vibecode.domain.auth.application.usecase.EnterUseCase;
import com.yd.vibecode.domain.auth.application.usecase.MeUseCase;
import com.yd.vibecode.domain.auth.domain.service.RefreshTokenService;
import com.yd.vibecode.global.annotation.AccessToken;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.security.JwtProperties;
import com.yd.vibecode.global.security.TokenProvider;
import com.yd.vibecode.global.swagger.AuthApi;
import com.yd.vibecode.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final EnterUseCase enterUseCase;
    private final AdminSignupUseCase adminSignupUseCase;
    private final AdminLoginUseCase adminLoginUseCase;
    private final AdminLogoutUseCase adminLogoutUseCase;
    private final MeUseCase meUseCase;
    private final CookieUtils cookieUtils;
    private final JwtProperties jwtProperties;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/enter")
    public BaseResponse<EnterResponse> enter(
            @Valid @RequestBody EnterRequest request,
            HttpServletResponse httpResponse) {
        EnterResponse response = enterUseCase.execute(request);
        int maxAge = Math.toIntExact(jwtProperties.getAccessTokenExpirationPeriodDay() / 1000);
        cookieUtils.setAccessTokenCookie(httpResponse, response.accessToken(), maxAge);
        // accessToken is intentionally kept in the response body so the FE can store it
        // in Zustand memory for STOMP WebSocket authentication (HttpOnly cookies are
        // not readable by JavaScript and therefore cannot be used in STOMP connectHeaders).
        return BaseResponse.onSuccess(response);
    }

    @PostMapping("/admin/signup")
    public BaseResponse<Void> adminSignup(@Valid @RequestBody AdminSignupRequest request) {
        adminSignupUseCase.execute(request);
        return BaseResponse.onSuccess();
    }

    @PostMapping("/admin/login")
    public BaseResponse<AdminLoginResponse> adminLogin(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse httpResponse) {
        AdminLoginResponse response = adminLoginUseCase.execute(request);
        int accessMaxAge = Math.toIntExact(jwtProperties.getAccessTokenExpirationPeriodDay() / 1000);
        cookieUtils.setAccessTokenCookie(httpResponse, response.accessToken(), accessMaxAge);

        // admin 전용 refresh token 발급: HttpOnly 쿠키로만 전달 (body 노출 불필요)
        String adminId = tokenProvider.getId(response.accessToken()).orElseThrow();
        String refreshToken = tokenProvider.createRefreshToken(adminId);
        Duration refreshDuration = Duration.ofMillis(jwtProperties.getRefreshTokenExpirationPeriodDay());
        refreshTokenService.saveRefreshToken(adminId, refreshToken, refreshDuration);
        int refreshMaxAge = Math.toIntExact(jwtProperties.getRefreshTokenExpirationPeriodDay() / 1000);
        cookieUtils.setRefreshTokenCookie(httpResponse, refreshToken, refreshMaxAge);

        return BaseResponse.onSuccess(response);
    }

    @PostMapping("/admin/logout")
    public BaseResponse<Void> adminLogout(
            @AccessToken String token,
            HttpServletResponse httpResponse) {
        adminLogoutUseCase.execute(token);
        cookieUtils.clearAccessTokenCookie(httpResponse);
        cookieUtils.clearRefreshTokenCookie(httpResponse);
        return BaseResponse.onSuccess();
    }

    @PostMapping("/admin/reissue")
    public BaseResponse<Void> adminReissue(HttpServletRequest request, HttpServletResponse httpResponse) {
        String refreshToken = cookieUtils.getRefreshTokenFromRequest(request);
        if (refreshToken == null) {
            throw new RestApiException(AuthErrorStatus.EMPTY_JWT);
        }
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RestApiException(AuthErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        String adminId = tokenProvider.getId(refreshToken)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN));

        if (!refreshTokenService.isExist(refreshToken, adminId)) {
            throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        // 토큰 로테이션: 기존 refresh token 삭제 후 신규 발급
        refreshTokenService.deleteRefreshToken(adminId);
        String newAccessToken = tokenProvider.createAccessToken(adminId, "ADMIN");
        String newRefreshToken = tokenProvider.createRefreshToken(adminId);
        Duration remaining = tokenProvider.getRemainingDuration(refreshToken)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.EXPIRED_REFRESH_TOKEN));
        refreshTokenService.saveRefreshToken(adminId, newRefreshToken, remaining);

        int accessMaxAge = Math.toIntExact(jwtProperties.getAccessTokenExpirationPeriodDay() / 1000);
        int refreshMaxAge = (int) Math.min(remaining.getSeconds(), Integer.MAX_VALUE);
        cookieUtils.setAccessTokenCookie(httpResponse, newAccessToken, accessMaxAge);
        cookieUtils.setRefreshTokenCookie(httpResponse, newRefreshToken, refreshMaxAge);

        return BaseResponse.onSuccess();
    }

    @GetMapping("/me")
    public BaseResponse<MeResponse> me(@AccessToken String token) {
        MeResponse response = meUseCase.execute(token);
        return BaseResponse.onSuccess(response);
    }
}

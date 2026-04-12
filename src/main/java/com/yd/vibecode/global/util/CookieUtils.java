package com.yd.vibecode.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    @Value("${cookie.secure:true}")
    private boolean secure;

    /**
     * HttpOnly access_token 쿠키를 응답에 추가한다.
     *
     * @param response      HttpServletResponse
     * @param token         JWT access token 값
     * @param maxAgeSeconds 만료 시간 (초)
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        // SameSite는 Servlet API로 설정 불가 → Set-Cookie 헤더로 직접 구성 (단일 경로)
        String cookieHeader = buildSetCookieHeader(token, maxAgeSeconds);
        response.addHeader("Set-Cookie", cookieHeader);
    }

    /**
     * access_token 쿠키를 삭제한다 (maxAge=0).
     *
     * @param response HttpServletResponse
     */
    public void clearAccessTokenCookie(HttpServletResponse response) {
        String cookieHeader = buildSetCookieHeader("", 0);
        response.addHeader("Set-Cookie", cookieHeader);
    }

    private String buildSetCookieHeader(String value, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(ACCESS_TOKEN_COOKIE_NAME).append("=").append(value);
        sb.append("; Path=/");
        sb.append("; Max-Age=").append(maxAge);
        sb.append("; HttpOnly");
        sb.append("; SameSite=Strict");
        if (secure) {
            sb.append("; Secure");
        }
        return sb.toString();
    }
}

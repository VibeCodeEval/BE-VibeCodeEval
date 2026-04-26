package com.yd.vibecode.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${cookie.secure:true}")
    private boolean secure;

    public void setAccessTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        response.addHeader("Set-Cookie", buildSetCookieHeader(ACCESS_TOKEN_COOKIE_NAME, token, maxAgeSeconds));
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildSetCookieHeader(ACCESS_TOKEN_COOKIE_NAME, "", 0));
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        response.addHeader("Set-Cookie", buildSetCookieHeader(REFRESH_TOKEN_COOKIE_NAME, token, maxAgeSeconds));
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildSetCookieHeader(REFRESH_TOKEN_COOKIE_NAME, "", 0));
    }

    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    private String buildSetCookieHeader(String name, String value, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
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

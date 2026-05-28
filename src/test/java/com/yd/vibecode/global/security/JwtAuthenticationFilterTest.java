package com.yd.vibecode.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.yd.vibecode.domain.auth.domain.service.AdminAccessAuthValidator;
import com.yd.vibecode.domain.auth.domain.service.RefreshTokenService;
import com.yd.vibecode.domain.auth.domain.service.TokenWhitelistService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private TokenProvider tokenProvider;
  @Mock private ExcludeAuthPathProperties excludeAuthPathProperties;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private TokenWhitelistService tokenWhitelistService;
  @Mock private AdminAccessAuthValidator adminAccessAuthValidator;
  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter =
        new JwtAuthenticationFilter(
            tokenProvider,
            excludeAuthPathProperties,
            refreshTokenService,
            tokenWhitelistService,
            adminAccessAuthValidator);
    SecurityContextHolder.clearContext();
    given(excludeAuthPathProperties.getPaths()).willReturn(Collections.emptyList());
  }

  @Test
  @DisplayName("삭제된 관리자 access token으로 보호 API 접근 시 401")
  void deletedAdminAccessToken_returnsUnauthorized() throws Exception {
    String token = "deleted-admin-token";
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/admin-numbers/admins");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(tokenProvider.getToken(request)).willReturn(Optional.of(token));
    given(tokenWhitelistService.isWhitelistToken(token)).willReturn(false);
    given(tokenProvider.validateToken(token)).willReturn(true);
    given(tokenProvider.getRole(token)).willReturn(Optional.of("ADMIN"));
    given(tokenProvider.getId(token)).willReturn(Optional.of("42"));
    willThrow(new RestApiException(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE))
        .given(adminAccessAuthValidator)
        .validateActiveForAuthentication(42L);

    filter.doFilter(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(any(), any());
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("활성 관리자 access token은 인증 성공")
  void activeAdminAccessToken_authenticates() throws Exception {
    String token = "active-admin-token";
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/admin-numbers/admins");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(tokenProvider.getToken(request)).willReturn(Optional.of(token));
    given(tokenWhitelistService.isWhitelistToken(token)).willReturn(false);
    given(tokenProvider.validateToken(token)).willReturn(true);
    given(tokenProvider.getRole(token)).willReturn(Optional.of("ADMIN"));
    given(tokenProvider.getId(token)).willReturn(Optional.of("1"));
    given(tokenProvider.getAuthentication(token))
        .willReturn(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "1", "", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));

    filter.doFilter(request, response, filterChain);

    verify(adminAccessAuthValidator).validateActiveForAuthentication(1L);
    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }

  @Test
  @DisplayName("USER role access token은 관리자 활성 검증을 하지 않는다")
  void userAccessToken_skipsAdminValidation() throws Exception {
    String token = "user-token";
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/exams/1/session");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(tokenProvider.getToken(request)).willReturn(Optional.of(token));
    given(tokenWhitelistService.isWhitelistToken(token)).willReturn(false);
    given(tokenProvider.validateToken(token)).willReturn(true);
    given(tokenProvider.getRole(token)).willReturn(Optional.of("USER"));
    given(tokenProvider.getAuthentication(token))
        .willReturn(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "10", "", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))));

    filter.doFilter(request, response, filterChain);

    verify(adminAccessAuthValidator, never()).validateActiveForAuthentication(any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("whitelist 캐시된 관리자 토큰도 활성 검증을 수행한다")
  void whitelistedDeletedAdminToken_returnsUnauthorized() throws Exception {
    String token = "whitelisted-deleted-admin-token";
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/exams");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(tokenProvider.getToken(request)).willReturn(Optional.of(token));
    given(tokenWhitelistService.isWhitelistToken(token)).willReturn(true);
    given(tokenProvider.getRole(token)).willReturn(Optional.of("MASTER"));
    given(tokenProvider.getId(token)).willReturn(Optional.of("99"));
    willThrow(new RestApiException(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE))
        .given(adminAccessAuthValidator)
        .validateActiveForAuthentication(99L);

    filter.doFilter(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    verify(adminAccessAuthValidator).validateActiveForAuthentication(eq(99L));
    verify(filterChain, never()).doFilter(any(), any());
  }
}

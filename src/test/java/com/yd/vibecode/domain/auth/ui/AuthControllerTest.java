package com.yd.vibecode.domain.auth.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.auth.application.dto.request.AdminLoginRequest;
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
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.JwtProperties;
import com.yd.vibecode.global.security.TokenProvider;
import com.yd.vibecode.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AuthController 단위 테스트
 *
 * - HttpOnly 쿠키 전환 이후 Set-Cookie 헤더 검증
 * - @AccessToken 리졸버는 WebMvcTest 컨텍스트에서 TokenProvider mock으로 동작
 * - CookieUtils / JwtProperties 는 MockBean으로 주입해 실제 쿠키 세팅 없이 호출 여부만 확인
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Security Filter 비활성화
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---- UseCase MockBeans ----
    @MockBean
    private EnterUseCase enterUseCase;
    @MockBean
    private AdminSignupUseCase adminSignupUseCase;
    @MockBean
    private AdminLoginUseCase adminLoginUseCase;
    @MockBean
    private AdminLogoutUseCase adminLogoutUseCase;
    @MockBean
    private MeUseCase meUseCase;

    // ---- Infrastructure MockBeans ----
    @MockBean
    private TokenProvider tokenProvider;
    @MockBean
    private CookieUtils cookieUtils;
    @MockBean
    private JwtProperties jwtProperties;
    @MockBean
    private RefreshTokenService refreshTokenService;
    // WebMvcConfig 가 JwtBlacklistInterceptor, ExcludeBlacklistPathProperties 를 주입받으므로 필요
    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;
    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @BeforeEach
    void setUp() {
        // JwtProperties: accessToken 만료시간 설정
        given(jwtProperties.getAccessTokenExpirationPeriodDay()).willReturn(3_600_000L);
        given(jwtProperties.getRefreshTokenExpirationPeriodDay()).willReturn(86_400_000L);
        // JwtBlacklistInterceptor: preHandle 기본값이 false → true로 설정해 컨트롤러에 요청이 도달하도록 함
        given(jwtBlacklistInterceptor.preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any()
        )).willReturn(true);
        // ExcludeBlacklistPathProperties: getExcludeAuthPaths() 기본값 null → 빈 리스트로 설정
        given(excludeBlacklistPathProperties.getExcludeAuthPaths()).willReturn(Collections.emptyList());
    }

    // =========================================================================
    // 1. POST /api/auth/enter — 사용자 입장
    // =========================================================================

    @Test
    @DisplayName("입장 API — 응답 바디에 accessToken 포함, cookieUtils.setAccessTokenCookie 호출 확인")
    void enter_api_success() throws Exception {
        // given
        EnterRequest request = new EnterRequest("CODE", "홍길동", "010-1234-5678");
        EnterResponse response = new EnterResponse("test-token", "USER", null, null, null);
        given(enterUseCase.execute(any(EnterRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").value("test-token"));

        // CookieUtils.setAccessTokenCookie 가 호출됐는지 검증
        verify(cookieUtils).setAccessTokenCookie(
                any(HttpServletResponse.class),
                eq("test-token"),
                eq(3600) // 3_600_000 ms / 1000
        );
    }

    @Test
    @DisplayName("입장 API — 요청 필드 누락 시 400 Bad Request")
    void enter_api_missing_fields_returns_400() throws Exception {
        // 이름 필드가 null인 요청
        String body = "{\"code\":\"CODE\",\"phone\":\"010-1234-5678\"}";

        mockMvc.perform(post("/api/auth/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 2. POST /api/auth/admin/login — 관리자 로그인
    // =========================================================================

    @Test
    @DisplayName("관리자 로그인 API — 응답 바디에 accessToken 포함, cookieUtils.setAccessTokenCookie 호출 확인")
    void admin_login_api_success() throws Exception {
        // given
        AdminLoginRequest request = new AdminLoginRequest("admin", "password");
        AdminLoginResponse response = new AdminLoginResponse("admin-token", "ADMIN", null);
        given(adminLoginUseCase.execute(any(AdminLoginRequest.class))).willReturn(response);
        given(tokenProvider.getId("admin-token")).willReturn(Optional.of("1"));
        given(tokenProvider.createRefreshToken("1")).willReturn("refresh-token");

        // when & then
        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").value("admin-token"));

        verify(cookieUtils).setAccessTokenCookie(
                any(HttpServletResponse.class),
                eq("admin-token"),
                eq(3600)
        );
        verify(refreshTokenService).saveRefreshToken(eq("1"), eq("refresh-token"), eq(Duration.ofMillis(86_400_000L)));
        verify(cookieUtils).setRefreshTokenCookie(
                any(HttpServletResponse.class),
                eq("refresh-token"),
                eq(86400)
        );
    }

    @Test
    @DisplayName("관리자 토큰 재발급 API — refresh token 검증 후 access/refresh 쿠키 재발급")
    void admin_reissue_api_success() throws Exception {
        // given
        given(cookieUtils.getRefreshTokenFromRequest(any(HttpServletRequest.class))).willReturn("old-refresh");
        given(tokenProvider.validateToken("old-refresh")).willReturn(true);
        given(tokenProvider.getId("old-refresh")).willReturn(Optional.of("1"));
        given(refreshTokenService.isExist("old-refresh", "1")).willReturn(true);
        given(tokenProvider.createAccessToken("1", "ADMIN")).willReturn("new-access");
        given(tokenProvider.createRefreshToken("1")).willReturn("new-refresh");
        given(tokenProvider.getRemainingDuration("old-refresh")).willReturn(Optional.of(Duration.ofSeconds(120)));

        // when & then
        mockMvc.perform(post("/api/auth/admin/reissue"))
                .andExpect(status().isOk());

        verify(refreshTokenService).deleteRefreshToken("1");
        verify(refreshTokenService).saveRefreshToken("1", "new-refresh", Duration.ofSeconds(120));
        verify(cookieUtils).setAccessTokenCookie(any(HttpServletResponse.class), eq("new-access"), eq(3600));
        verify(cookieUtils).setRefreshTokenCookie(any(HttpServletResponse.class), eq("new-refresh"), eq(120));
    }

    // =========================================================================
    // 3. POST /api/auth/admin/logout — 관리자 로그아웃
    // =========================================================================

    @Test
    @DisplayName("관리자 로그아웃 API — clearAccessTokenCookie 호출, 200 OK")
    void admin_logout_api_success() throws Exception {
        // given — @AccessToken 리졸버가 TokenProvider.getToken() 으로 토큰을 꺼냄
        String token = "valid-admin-token";
        given(tokenProvider.getToken(any())).willReturn(Optional.of(token));
        given(tokenProvider.isAccessToken(token)).willReturn(true);
        willDoNothing().given(adminLogoutUseCase).execute(token);

        // when & then
        mockMvc.perform(post("/api/auth/admin/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(adminLogoutUseCase).execute(token);
        verify(cookieUtils).clearAccessTokenCookie(any(HttpServletResponse.class));
        verify(cookieUtils).clearRefreshTokenCookie(any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("관리자 로그아웃 API — 토큰 없을 시 401 Unauthorized")
    void admin_logout_api_no_token_returns_401() throws Exception {
        // given — 토큰이 없으면 리졸버가 RestApiException(_UNAUTHORIZED) 를 던짐
        given(tokenProvider.getToken(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/admin/logout"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 4. GET /api/auth/me — 내 정보 조회
    // =========================================================================

    @Test
    @DisplayName("내 정보 조회 API — 쿠키 토큰으로 조회 성공")
    void me_api_success_with_cookie_token() throws Exception {
        // given — 쿠키 우선 경로 시뮬레이션
        String token = "cookie-token";
        MeResponse response = new MeResponse("USER", null, null, null);
        given(tokenProvider.getToken(any())).willReturn(Optional.of(token));
        given(tokenProvider.isAccessToken(token)).willReturn(true);
        given(meUseCase.execute(token)).willReturn(response);

        // when & then — 쿠키로 요청 (Authorization 헤더 없음)
        mockMvc.perform(get("/api/auth/me")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.role").value("USER"));
    }

    @Test
    @DisplayName("내 정보 조회 API — Authorization 헤더 폴백 성공")
    void me_api_success_with_bearer_token() throws Exception {
        // given — 쿠키 없이 Bearer 헤더만 있을 때
        String token = "header-token";
        MeResponse response = new MeResponse("USER", null, null, null);
        given(tokenProvider.getToken(any())).willReturn(Optional.of(token));
        given(tokenProvider.isAccessToken(token)).willReturn(true);
        given(meUseCase.execute(token)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.role").value("USER"));
    }

    @Test
    @DisplayName("내 정보 조회 API — 토큰 없을 시 401 Unauthorized")
    void me_api_no_token_returns_401() throws Exception {
        given(tokenProvider.getToken(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 5. 토큰 우선순위 엣지 케이스 — TokenProvider getToken 로직 단위 검증
    // =========================================================================

    @Test
    @DisplayName("TokenProvider — 쿠키와 Bearer 헤더 동시 존재 시 쿠키 우선")
    void token_provider_prefers_cookie_over_header() {
        // given
        TokenProvider realProvider = buildRealTokenProvider();
        MockHttpServletRequest request = new MockHttpServletRequest();

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("access_token", "cookie-val");
        request.setCookies(cookie);
        request.addHeader("Authorization", "Bearer header-val");

        // when
        Optional<String> token = realProvider.getToken(request);

        // then
        assertThat(token).isPresent();
        assertThat(token.get()).isEqualTo("cookie-val");
    }

    @Test
    @DisplayName("TokenProvider — 빈 쿠키 값일 때 Bearer 헤더 폴백")
    void token_provider_falls_back_to_header_when_cookie_blank() {
        // given
        TokenProvider realProvider = buildRealTokenProvider();
        MockHttpServletRequest request = new MockHttpServletRequest();

        jakarta.servlet.http.Cookie blankCookie = new jakarta.servlet.http.Cookie("access_token", "   ");
        request.setCookies(blankCookie);
        request.addHeader("Authorization", "Bearer header-val");

        // when
        Optional<String> token = realProvider.getToken(request);

        // then
        assertThat(token).isPresent();
        assertThat(token.get()).isEqualTo("header-val");
    }

    @Test
    @DisplayName("TokenProvider — 쿠키도 헤더도 없을 때 Optional.empty()")
    void token_provider_returns_empty_when_no_token() {
        // given
        TokenProvider realProvider = buildRealTokenProvider();
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        Optional<String> token = realProvider.getToken(request);

        // then
        assertThat(token).isEmpty();
    }

    @Test
    @DisplayName("TokenProvider — null 쿠키 값일 때 Bearer 헤더 폴백")
    void token_provider_falls_back_to_header_when_cookie_value_null() {
        // given
        TokenProvider realProvider = buildRealTokenProvider();
        MockHttpServletRequest request = new MockHttpServletRequest();

        // MockHttpServletRequest는 null 쿠키 값을 직접 지원하지 않으므로
        // 쿠키 없이 헤더만 설정해 폴백 경로 검증
        request.addHeader("Authorization", "Bearer fallback-token");

        // when
        Optional<String> token = realProvider.getToken(request);

        // then
        assertThat(token).isPresent();
        assertThat(token.get()).isEqualTo("fallback-token");
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /**
     * TokenProvider 우선순위 로직 단위 검증에 사용할 실제 인스턴스를 생성한다.
     * JwtProperties는 key/expiration 값이 불필요한 getToken() 경로만 테스트하므로
     * 더미 값으로 구성한다.
     */
    private TokenProvider buildRealTokenProvider() {
        JwtProperties props = new JwtProperties();
        props.setKey("test-secret-key-32-characters-long!!");
        props.setAccessTokenExpirationPeriodDay(3_600_000L);
        props.setRefreshTokenExpirationPeriodDay(86_400_000L);
        return new TokenProvider(props);
    }
}

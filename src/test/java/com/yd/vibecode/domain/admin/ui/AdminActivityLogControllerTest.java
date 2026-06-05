package com.yd.vibecode.domain.admin.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.yd.vibecode.domain.admin.application.dto.response.AdminActivityLogPageResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetAdminActivityLogsUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
        controllers = AdminActivityLogController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminActivityLogControllerTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final Long CURRENT_USER_ID = 42L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAdminActivityLogsUseCase getAdminActivityLogsUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        given(jwtBlacklistInterceptor.preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any()))
                .willReturn(true);
        given(excludeBlacklistPathProperties.getExcludeAuthPaths()).willReturn(Collections.emptyList());
        given(tokenProvider.getToken(any(HttpServletRequest.class))).willReturn(Optional.of(ACCESS_TOKEN));
        given(tokenProvider.isAccessToken(ACCESS_TOKEN)).willReturn(true);
        given(tokenProvider.getId(ACCESS_TOKEN)).willReturn(Optional.of(String.valueOf(CURRENT_USER_ID)));
    }

    @Test
    @DisplayName("관리자 활동 로그 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getLogs_success() throws Exception {
        given(getAdminActivityLogsUseCase.execute(
                org.mockito.ArgumentMatchers.eq(CURRENT_USER_ID),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(0),
                org.mockito.ArgumentMatchers.eq(20)))
                .willReturn(new AdminActivityLogPageResponse(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isOk());
    }
}

package com.yd.vibecode.domain.admin.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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

import com.yd.vibecode.domain.admin.application.dto.response.MasterActivityLogPageResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetMasterActivityLogsUseCase;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
        controllers = MasterActivityLogController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class MasterActivityLogControllerTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetMasterActivityLogsUseCase getMasterActivityLogsUseCase;

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
    @DisplayName("MASTER - 마스터 활동 로그 조회 성공")
    @WithMockUser(roles = "MASTER")
    void getLogs_master_success() throws Exception {
        given(getMasterActivityLogsUseCase.execute(
                eq(CURRENT_USER_ID),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                eq(0),
                eq(20)))
                .willReturn(new MasterActivityLogPageResponse(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/admin/master/logs")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일반 ADMIN - MASTER_ONLY로 조회 거부")
    @WithMockUser(roles = "ADMIN")
    void getLogs_admin_forbidden() throws Exception {
        willThrow(new RestApiException(AuthErrorStatus.MASTER_ONLY))
                .given(getMasterActivityLogsUseCase)
                .execute(eq(CURRENT_USER_ID), any(), any(), eq(0), eq(20));

        mockMvc.perform(get("/api/admin/master/logs")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isForbidden());
    }
}

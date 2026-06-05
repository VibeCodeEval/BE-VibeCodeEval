package com.yd.vibecode.domain.admin.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collections;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.admin.application.dto.request.UpdateMasterPlatformSettingsRequest;
import com.yd.vibecode.domain.admin.application.dto.response.MasterPlatformSettingsResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetMasterPlatformSettingsUseCase;
import com.yd.vibecode.domain.admin.application.usecase.UpdateMasterPlatformSettingsUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
        controllers = MasterPlatformSettingsController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class MasterPlatformSettingsControllerTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetMasterPlatformSettingsUseCase getMasterPlatformSettingsUseCase;

    @MockBean
    private UpdateMasterPlatformSettingsUseCase updateMasterPlatformSettingsUseCase;

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
    @DisplayName("MASTER - 플랫폼 설정 조회 성공")
    @WithMockUser(roles = "MASTER")
    void getSettings_success() throws Exception {
        given(getMasterPlatformSettingsUseCase.execute(CURRENT_USER_ID))
                .willReturn(new MasterPlatformSettingsResponse(90, 90, true, LocalDateTime.now()));

        mockMvc.perform(get("/api/admin/master/settings")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MASTER - 플랫폼 설정 수정 성공")
    @WithMockUser(roles = "MASTER")
    void updateSettings_success() throws Exception {
        UpdateMasterPlatformSettingsRequest request = new UpdateMasterPlatformSettingsRequest(
                90, 90, true);
        given(updateMasterPlatformSettingsUseCase.execute(eq(CURRENT_USER_ID), any(UpdateMasterPlatformSettingsRequest.class)))
                .willReturn(new MasterPlatformSettingsResponse(90, 90, true, LocalDateTime.now()));

        mockMvc.perform(put("/api/admin/master/settings")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}

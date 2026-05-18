package com.yd.vibecode.domain.admin.ui;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.yd.vibecode.domain.admin.application.dto.response.SystemStatusResponse;
import com.yd.vibecode.domain.admin.application.dto.response.SystemStatusResponse.ServiceStatusItem;
import com.yd.vibecode.domain.admin.application.usecase.GetSystemStatusUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
    controllers = AdminSystemStatusController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminSystemStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetSystemStatusUseCase getSystemStatusUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("시스템 상태 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getSystemStatus_success() throws Exception {
        given(getSystemStatusUseCase.execute())
            .willReturn(new SystemStatusResponse(List.of(
                new ServiceStatusItem("api", "API 서버", "UP", 1L),
                new ServiceStatusItem("database", "데이터베이스", "UP", 8L),
                new ServiceStatusItem("ai", "AI 게이트웨이", "DOWN", null)
            )));

        mockMvc.perform(get("/api/admin/system-status"))
            .andExpect(status().isOk());
    }
}

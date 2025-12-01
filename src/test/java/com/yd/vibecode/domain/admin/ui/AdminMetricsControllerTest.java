package com.yd.vibecode.domain.admin.ui;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.yd.vibecode.domain.admin.application.dto.response.AdminMetricsResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetAdminMetricsUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
    controllers = AdminMetricsController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAdminMetricsUseCase getAdminMetricsUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("운영 메트릭 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getMetrics_success() throws Exception {
        // given
        Long examId = 1L;
        AdminMetricsResponse mockResponse = new AdminMetricsResponse(
            new AdminMetricsResponse.ConcurrencyMetrics(100, 105),
            new AdminMetricsResponse.QueueMetrics(5, 2.0),
            new AdminMetricsResponse.ErrorMetrics(0.1, "NONE")
        );
        
        given(getAdminMetricsUseCase.execute(eq(examId)))
            .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/admin/metrics")
                .param("examId", String.valueOf(examId)))
            .andExpect(status().isOk());
    }
}

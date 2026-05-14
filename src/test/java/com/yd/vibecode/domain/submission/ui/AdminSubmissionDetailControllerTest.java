package com.yd.vibecode.domain.submission.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;

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

import com.yd.vibecode.domain.submission.application.dto.response.AdminSubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.usecase.GetAdminSubmissionDetailUseCase;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
        controllers = AdminSubmissionDetailController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminSubmissionDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAdminSubmissionDetailUseCase getAdminSubmissionDetailUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUpInterceptor() {
        // Mock JwtBlacklistInterceptor: Mockito boolean 기본값 false → true (컨트롤러까지 요청 전달)
        given(jwtBlacklistInterceptor.preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any())).willReturn(true);
        given(excludeBlacklistPathProperties.getExcludeAuthPaths()).willReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("관리자 제출 상세 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getAdminSubmissionDetail_success() throws Exception {
        Long submissionId = 42L;
        AdminSubmissionDetailResponse body = new AdminSubmissionDetailResponse(
                submissionId,
                SubmissionStatus.DONE,
                "python3.11",
                "print(1)",
                new SubmissionDetailResponse.MetricsInfo(10, 512, 1),
                new SubmissionDetailResponse.TestCaseInfo(1.0, java.util.List.of()),
                new SubmissionDetailResponse.ScoreInfo(
                        new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"), new BigDecimal("6")),
                "{\"rubric\":true}");

        given(getAdminSubmissionDetailUseCase.execute(eq(submissionId))).willReturn(body);

        mockMvc.perform(get("/api/admin/submissions/" + submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.submissionId").value(42))
                .andExpect(jsonPath("$.result.codeInline").value("print(1)"))
                .andExpect(jsonPath("$.result.rubricJson").value("{\"rubric\":true}"));
    }
}

package com.yd.vibecode.domain.submission.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.yd.vibecode.domain.submission.application.dto.response.AdminSubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.usecase.GetAdminSubmissionDetailUseCase;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;
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

  private static final Long ADMIN_USER_ID = 1L;

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
  void setUp() throws Exception {
    given(jwtBlacklistInterceptor.preHandle(
            any(HttpServletRequest.class),
            any(HttpServletResponse.class),
            any())).willReturn(true);
    given(excludeBlacklistPathProperties.getExcludeAuthPaths()).willReturn(Collections.emptyList());
    given(tokenProvider.getToken(any(HttpServletRequest.class)))
        .willReturn(Optional.of("mock-token"));
    given(tokenProvider.isAccessToken("mock-token")).willReturn(true);
    given(tokenProvider.getId("mock-token")).willReturn(Optional.of(String.valueOf(ADMIN_USER_ID)));
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
        "{\"rubric\":true}",
        java.util.List.of());

    given(getAdminSubmissionDetailUseCase.execute(eq(ADMIN_USER_ID), eq(submissionId))).willReturn(body);

    mockMvc.perform(get("/api/admin/submissions/" + submissionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.submissionId").value(42))
        .andExpect(jsonPath("$.result.codeInline").value("print(1)"))
        .andExpect(jsonPath("$.result.rubricJson").value("{\"rubric\":true}"));
  }

  @Test
  @DisplayName("관리자 권한 없음(UseCase) — 403")
  @WithMockUser(roles = "ADMIN")
  void getAdminSubmissionDetail_forbidden() throws Exception {
    Long submissionId = 42L;
    given(getAdminSubmissionDetailUseCase.execute(eq(ADMIN_USER_ID), eq(submissionId)))
        .willThrow(new RestApiException(AuthErrorStatus.FORBIDDEN));

    mockMvc.perform(get("/api/admin/submissions/" + submissionId))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("AUTH015"));
  }

  @Test
  @DisplayName("존재하지 않는 submissionId — 404")
  @WithMockUser(roles = "ADMIN")
  void getAdminSubmissionDetail_notFound() throws Exception {
    Long submissionId = 999L;
    given(getAdminSubmissionDetailUseCase.execute(eq(ADMIN_USER_ID), eq(submissionId)))
        .willThrow(new RestApiException(SubmissionErrorStatus.SUBMISSION_NOT_FOUND));

    mockMvc.perform(get("/api/admin/submissions/" + submissionId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("SUB001"));
  }
}

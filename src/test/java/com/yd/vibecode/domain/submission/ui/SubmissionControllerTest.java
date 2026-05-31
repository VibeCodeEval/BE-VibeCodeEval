package com.yd.vibecode.domain.submission.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.yd.vibecode.domain.exam.application.usecase.GetParticipantCodeDraftUseCase;
import com.yd.vibecode.domain.exam.application.usecase.SaveParticipantCodeDraftUseCase;
import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.usecase.GetSubmissionDetailUseCase;
import com.yd.vibecode.domain.submission.application.usecase.SubmitUseCase;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.global.config.WebMvcConfig;
import com.yd.vibecode.global.exception.ExceptionAdvice;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
        controllers = SubmissionController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import({WebMvcConfig.class, ExceptionAdvice.class})
class SubmissionControllerTest {

    private static final Long CURRENT_USER_ID = 100L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubmitUseCase submitUseCase;

    @MockBean
    private GetSubmissionDetailUseCase getSubmissionDetailUseCase;

    @MockBean
    private SaveParticipantCodeDraftUseCase saveParticipantCodeDraftUseCase;

    @MockBean
    private GetParticipantCodeDraftUseCase getParticipantCodeDraftUseCase;

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
                .willReturn(Optional.of("access-token"));
        given(tokenProvider.isAccessToken("access-token")).willReturn(true);
        given(tokenProvider.getId("access-token"))
                .willReturn(Optional.of(String.valueOf(CURRENT_USER_ID)));
    }

    @Test
    @DisplayName("본인 제출 상세 조회 성공 — 200")
    void getSubmissionDetail_success() throws Exception {
        Long submissionId = 1L;
        SubmissionDetailResponse body = new SubmissionDetailResponse(
                submissionId,
                SubmissionStatus.DONE,
                "python3.11",
                new SubmissionDetailResponse.MetricsInfo(150, 2048, 10),
                new SubmissionDetailResponse.TestCaseInfo(0.4, List.of()),
                new SubmissionDetailResponse.ScoreInfo(
                        new BigDecimal("30"), new BigDecimal("20"), new BigDecimal("10"), new BigDecimal("60")));

        given(getSubmissionDetailUseCase.execute(eq(CURRENT_USER_ID), eq(submissionId))).willReturn(body);

        mockMvc.perform(get("/api/submissions/" + submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("DONE"));
    }

    @Test
    @DisplayName("타인 제출 조회 — 403")
    void getSubmissionDetail_forbidden() throws Exception {
        Long submissionId = 1L;
        given(getSubmissionDetailUseCase.execute(eq(CURRENT_USER_ID), eq(submissionId)))
                .willThrow(new RestApiException(GlobalErrorStatus._FORBIDDEN));

        mockMvc.perform(get("/api/submissions/" + submissionId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON403"));
    }

    @Test
    @DisplayName("존재하지 않는 submissionId — 404")
    void getSubmissionDetail_notFound() throws Exception {
        Long submissionId = 999L;
        given(getSubmissionDetailUseCase.execute(eq(CURRENT_USER_ID), eq(submissionId)))
                .willThrow(new RestApiException(SubmissionErrorStatus.SUBMISSION_NOT_FOUND));

        mockMvc.perform(get("/api/submissions/" + submissionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUB001"));
    }
}

package com.yd.vibecode.domain.exam.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yd.vibecode.domain.exam.application.dto.response.ActiveSessionResponse;
import com.yd.vibecode.domain.exam.application.usecase.GetActiveSessionUseCase;
import com.yd.vibecode.domain.exam.application.usecase.GetExamStateUseCase;
import com.yd.vibecode.domain.exam.application.usecase.GetParticipantSessionUseCase;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(
    controllers = ExamController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetExamStateUseCase getExamStateUseCase;

    @MockBean
    private GetParticipantSessionUseCase getParticipantSessionUseCase;

    @MockBean
    private GetActiveSessionUseCase getActiveSessionUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("활성 세션 조회 성공 — 200 OK와 세션 정보 반환")
    void getActiveSession_success() throws Exception {
        // given — JwtBlacklistInterceptor 통과, TokenProvider 모킹
        given(jwtBlacklistInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                .willReturn(true);
        given(tokenProvider.getToken(any(HttpServletRequest.class)))
                .willReturn(Optional.of("mock-token"));
        given(tokenProvider.isAccessToken("mock-token")).willReturn(true);
        given(tokenProvider.getId("mock-token"))
                .willReturn(Optional.of("100"));

        ActiveSessionResponse response = new ActiveSessionResponse(
            1L, 999L, 300L, 200L,
            ExamState.RUNNING,
            LocalDateTime.of(2026, 5, 6, 9, 0),
            LocalDateTime.of(2026, 5, 6, 11, 0),
            LocalDateTime.now(),
            50000, 1000
        );
        given(getActiveSessionUseCase.execute(100L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/exams/active-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.examId").value(1))
                .andExpect(jsonPath("$.result.examParticipantId").value(999))
                .andExpect(jsonPath("$.result.examState").value("RUNNING"));
    }

    @Test
    @DisplayName("활성 세션 조회 실패 — 활성 세션 없으면 404 반환")
    void getActiveSession_not_found() throws Exception {
        // given
        given(jwtBlacklistInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                .willReturn(true);
        given(tokenProvider.getToken(any(HttpServletRequest.class)))
                .willReturn(Optional.of("mock-token"));
        given(tokenProvider.isAccessToken("mock-token")).willReturn(true);
        given(tokenProvider.getId("mock-token"))
                .willReturn(Optional.of("200"));

        given(getActiveSessionUseCase.execute(200L))
                .willThrow(new RestApiException(ExamErrorStatus.NO_ACTIVE_SESSION));

        // when & then
        mockMvc.perform(get("/api/exams/active-session"))
                .andExpect(status().isNotFound());
    }
}

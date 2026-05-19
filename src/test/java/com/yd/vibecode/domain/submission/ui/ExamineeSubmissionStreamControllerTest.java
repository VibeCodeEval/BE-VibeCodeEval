package com.yd.vibecode.domain.submission.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;
import com.yd.vibecode.domain.submission.infrastructure.SseEmitterRegistry;
import com.yd.vibecode.global.config.WebMvcConfig;
import com.yd.vibecode.global.exception.ExceptionAdvice;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
        controllers = ExamineeSubmissionStreamController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import({WebMvcConfig.class, ExceptionAdvice.class})
class ExamineeSubmissionStreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubmissionService submissionService;

    @MockBean
    private SseEmitterRegistry sseEmitterRegistry;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        given(jwtBlacklistInterceptor.preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any())).willReturn(true);
        given(excludeBlacklistPathProperties.getExcludeAuthPaths()).willReturn(Collections.emptyList());
        given(tokenProvider.getToken(any(HttpServletRequest.class))).willReturn(Optional.of("access-token"));
        given(tokenProvider.isAccessToken("access-token")).willReturn(true);
    }

    @Test
    @DisplayName("소유자 불일치 시 403")
    void stream_forbiddenWhenNotOwner() throws Exception {
        given(tokenProvider.getId("access-token")).willReturn(Optional.of("100"));
        Submission submission = mock(Submission.class);
        given(submission.getParticipantId()).willReturn(99L);
        given(submissionService.findById(1L)).willReturn(submission);

        mockMvc.perform(get("/api/submissions/1/stream"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("소유자 일치 시 200 및 register 호출")
    void stream_okWhenOwner() throws Exception {
        given(tokenProvider.getId("access-token")).willReturn(Optional.of("100"));
        Submission submission = mock(Submission.class);
        given(submission.getParticipantId()).willReturn(100L);
        given(submissionService.findById(1L)).willReturn(submission);
        given(sseEmitterRegistry.register(eq(1L)))
                .willAnswer(inv -> new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(60_000L));

        mockMvc.perform(get("/api/submissions/1/stream"))
                .andExpect(status().isOk());

        verify(sseEmitterRegistry).register(1L);
    }
}

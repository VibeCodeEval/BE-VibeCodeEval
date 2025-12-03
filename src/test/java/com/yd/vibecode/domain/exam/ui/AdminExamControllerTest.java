package com.yd.vibecode.domain.exam.ui;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.yd.vibecode.domain.admin.application.usecase.GetExamsUseCase;
import com.yd.vibecode.domain.exam.application.usecase.EndExamUseCase;
import com.yd.vibecode.domain.exam.application.usecase.ExtendExamUseCase;
import com.yd.vibecode.domain.exam.application.usecase.StartExamUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
    controllers = AdminExamController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StartExamUseCase startExamUseCase;

    @MockBean
    private EndExamUseCase endExamUseCase;

    @MockBean
    private ExtendExamUseCase extendExamUseCase;

    @MockBean
    private GetExamsUseCase getExamsUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("모든 시험 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getExams_success() throws Exception {
        // given
        given(getExamsUseCase.execute())
            .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/admin/exams"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("시험 시작 성공")
    @WithMockUser(roles = "ADMIN")
    void startExam_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/admin/exams/1/start"))
            .andExpect(status().isOk());

    }

    @Test
    @DisplayName("시험 종료 성공")
    @WithMockUser(roles = "ADMIN")
    void endExam_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/admin/exams/1/end"))
            .andExpect(status().isOk());

    }

    @Test
    @DisplayName("시험 시간 연장 성공")
    @WithMockUser(roles = "ADMIN")
    void extendExam_success() throws Exception {
        // given
        String requestBody = """
            {
                "minutes": 10
            }
            """;

        // when & then
        mockMvc.perform(post("/api/admin/exams/1/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

    }
}

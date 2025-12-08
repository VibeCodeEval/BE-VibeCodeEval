package com.yd.vibecode.domain.admin.ui;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yd.vibecode.domain.admin.application.usecase.DeleteProblemUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemSpecsUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemsUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
    controllers = AdminProblemController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetProblemsUseCase getProblemsUseCase;

    @MockBean
    private DeleteProblemUseCase deleteProblemUseCase;

    @MockBean
    private GetProblemSpecsUseCase getProblemSpecsUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("문제 스펙 목록 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getProblemSpecs_success() throws Exception {
        // given
        Long problemId = 1L;
        given(getProblemSpecsUseCase.execute(eq(problemId)))
            .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/admin/problems/" + problemId + "/specs"))
            .andExpect(status().isOk());
    }
}

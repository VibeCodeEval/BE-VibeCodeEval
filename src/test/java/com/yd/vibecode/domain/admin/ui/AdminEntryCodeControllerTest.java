package com.yd.vibecode.domain.admin.ui;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.admin.application.usecase.CreateEntryCodeUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetEntryCodesUseCase;
import com.yd.vibecode.domain.admin.application.usecase.UpdateEntryCodeUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
    controllers = AdminEntryCodeController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminEntryCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateEntryCodeUseCase createEntryCodeUseCase;

    @MockBean
    private UpdateEntryCodeUseCase updateEntryCodeUseCase;

    @MockBean
    private GetEntryCodesUseCase getEntryCodesUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("입장 코드 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getEntryCodes_success() throws Exception {
        // given
        Long examId = 1L;
        Boolean isActive = true;
        EntryCodeResponse response = new EntryCodeResponse(
            "CODE123", examId, 100L, "Test Label", LocalDateTime.now().plusDays(1), 10, 0, true
        );

        given(getEntryCodesUseCase.execute(eq(examId), eq(isActive)))
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/admin/entry-codes")
                .param("examId", String.valueOf(examId))
                .param("isActive", String.valueOf(isActive)))
            .andExpect(status().isOk());
    }
}

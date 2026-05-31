package com.yd.vibecode.domain.admin.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

import com.yd.vibecode.domain.admin.application.usecase.ChangeAdminPasswordUseCase;
import com.yd.vibecode.domain.admin.application.usecase.DeleteOwnAdminAccountUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
    controllers = AdminAccountController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminAccountControllerTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChangeAdminPasswordUseCase changeAdminPasswordUseCase;

    @MockBean
    private DeleteOwnAdminAccountUseCase deleteOwnAdminAccountUseCase;

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
                any()
        )).willReturn(true);
        given(tokenProvider.getToken(any(HttpServletRequest.class))).willReturn(Optional.of(ACCESS_TOKEN));
        given(tokenProvider.isAccessToken(ACCESS_TOKEN)).willReturn(true);
        given(tokenProvider.getId(ACCESS_TOKEN)).willReturn(Optional.of(String.valueOf(CURRENT_USER_ID)));
    }

    @Test
    @DisplayName("관리자 비밀번호 변경 성공")
    @WithMockUser(roles = "ADMIN")
    void changePassword_success() throws Exception {
        // given
        String requestBody = """
            {
                "currentPassword": "current123!",
                "newPassword": "new123!!"
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/admin/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

    }

    @Test
    @DisplayName("관리자 본인 계정 삭제 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteOwnAccount_success() throws Exception {
        mockMvc.perform(delete("/api/admin/account")
                .header("Authorization", "Bearer " + ACCESS_TOKEN))
            .andExpect(status().isOk());

        verify(deleteOwnAdminAccountUseCase).execute(eq(CURRENT_USER_ID), eq(ACCESS_TOKEN), any());
    }
}

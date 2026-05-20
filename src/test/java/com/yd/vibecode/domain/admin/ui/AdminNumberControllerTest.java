package com.yd.vibecode.domain.admin.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import com.yd.vibecode.domain.admin.application.dto.response.AdminListResponse;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.domain.admin.application.dto.response.ResetAdminPasswordByMasterResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetAllAdminsUseCase;
import com.yd.vibecode.domain.admin.application.usecase.IssueAdminNumberUseCase;
import com.yd.vibecode.domain.admin.application.usecase.ResetAdminPasswordByMasterUseCase;
import com.yd.vibecode.domain.admin.application.usecase.UpdateAdminNumberUseCase;
import com.yd.vibecode.global.interceptor.JwtBlacklistInterceptor;
import com.yd.vibecode.global.security.ExcludeBlacklistPathProperties;
import com.yd.vibecode.global.security.SecurityConfig;
import com.yd.vibecode.global.security.TokenProvider;

@WebMvcTest(
    controllers = AdminNumberController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class AdminNumberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IssueAdminNumberUseCase issueAdminNumberUseCase;

    @MockBean
    private UpdateAdminNumberUseCase updateAdminNumberUseCase;

    @MockBean
    private GetAllAdminsUseCase getAllAdminsUseCase;

    @MockBean
    private ResetAdminPasswordByMasterUseCase resetAdminPasswordByMasterUseCase;

    @MockBean
    private JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @MockBean
    private ExcludeBlacklistPathProperties excludeBlacklistPathProperties;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("모든 관리자 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getAllAdmins_success() throws Exception {
        AdminListResponse mockResponse = new AdminListResponse(List.of());
        given(getAllAdminsUseCase.execute(any(Long.class)))
            .willReturn(mockResponse);

        mockMvc.perform(get("/api/admin/admin-numbers/admins"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자 번호 발급 성공")
    @WithMockUser(roles = "ADMIN")
    void issueAdminNumber_success() throws Exception {
        String requestBody = """
            {
                "label": "Test Admin",
                "expiresAt": "2025-12-31T23:59:59"
            }
            """;

        AdminNumberResponse mockResponse = new AdminNumberResponse(
            "ADM-123456", "Test Admin", true, 1L, null,
            LocalDateTime.parse("2025-12-31T23:59:59"), null, LocalDateTime.now()
        );

        given(issueAdminNumberUseCase.execute(any(Long.class), any()))
            .willReturn(mockResponse);

        mockMvc.perform(post("/api/admin/admin-numbers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자 번호 수정 성공")
    @WithMockUser(roles = "ADMIN")
    void updateAdminNumber_success() throws Exception {
        String adminNumber = "ADM-123456";
        String requestBody = """
            {
                "label": "Updated Admin",
                "active": false
            }
            """;

        AdminNumberResponse mockResponse = new AdminNumberResponse(
            adminNumber, "Updated Admin", false, 1L, null, null, null, LocalDateTime.now()
        );

        given(updateAdminNumberUseCase.execute(any(Long.class), eq(adminNumber), any()))
            .willReturn(mockResponse);

        mockMvc.perform(patch("/api/admin/admin-numbers/" + adminNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MASTER 타 관리자 임시 비밀번호 재설정 성공")
    @WithMockUser(roles = "ADMIN")
    void resetAdminPasswordByMaster_success() throws Exception {
        String adminNumber = "ADM-123456";

        given(jwtBlacklistInterceptor.preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any()
        )).willReturn(true);
        given(tokenProvider.getToken(any(HttpServletRequest.class))).willReturn(Optional.of("access-token"));
        given(tokenProvider.isAccessToken("access-token")).willReturn(true);
        given(tokenProvider.getId("access-token")).willReturn(Optional.of("1"));

        given(resetAdminPasswordByMasterUseCase.execute(eq(1L), eq(adminNumber)))
                .willReturn(new ResetAdminPasswordByMasterResponse("TempPass1!xYz"));

        mockMvc.perform(patch("/api/admin/admin-numbers/" + adminNumber + "/password/reset"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.temporaryPassword").value("TempPass1!xYz"));

        verify(resetAdminPasswordByMasterUseCase).execute(eq(1L), eq(adminNumber));
        verify(updateAdminNumberUseCase, never()).execute(any(Long.class), eq(adminNumber), any());
    }
}

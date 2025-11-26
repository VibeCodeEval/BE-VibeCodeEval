package com.yd.vibecode.domain.auth;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.config.TestConfig;
import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@Import(TestConfig.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntryCodeRepository entryCodeRepository;

    @BeforeEach
    void setUp() {
        // 테스트용 입장 코드 생성
        EntryCode entryCode = EntryCode.builder()
                .code("TEST-CODE")
                .examId(1L)
                .createdBy(1L)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .maxUses(100)
                .usedCount(0)
                .build();
        entryCodeRepository.save(entryCode);
    }

    @Test
    @DisplayName("통합 테스트: 입장 성공")
    void enter_integration_success() throws Exception {
        // given
        EnterRequest request = new EnterRequest("TEST-CODE", "테스트유저", "010-1111-2222");

        // when & then
        mockMvc.perform(post("/api/auth/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.participant.name").value("테스트유저"));
    }

    // TODO: Admin Signup Integration Test - SecretKey 검증 로직 확인 필요
    /*
    @Test
    @DisplayName("통합 테스트: 관리자 회원가입")
    void admin_signup_integration_success() throws Exception {
        // given
        AdminSignupRequest request = new AdminSignupRequest("admin999", "password123", "admin999@example.com");

        // when & then
        mockMvc.perform(post("/api/auth/admin/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
    */
}

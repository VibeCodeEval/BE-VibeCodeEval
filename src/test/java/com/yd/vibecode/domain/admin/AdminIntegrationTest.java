package com.yd.vibecode.domain.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.config.TestConfig;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.global.security.TokenProvider;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@Import(TestConfig.class)
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TokenProvider tokenProvider;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = Admin.builder()
            .adminNumber("admin123")
            .email("admin@example.com")
            .passwordHash(passwordEncoder.encode("password"))
            .is2faEnabled(false)
            .build();
        adminRepository.save(admin);
    }

    @Test
    @DisplayName("메트릭 조회 성공")
    void getMetrics_success() throws Exception {
        String token = tokenProvider.createAccessToken(admin.getId().toString(), "ADMIN");

        mockMvc.perform(get("/api/admin/metrics")
                        .param("examId", "1")
                        .header("Authorization", "Bearer " + token))
            .andDo(print())
            .andExpect(status().isOk());
    }
}

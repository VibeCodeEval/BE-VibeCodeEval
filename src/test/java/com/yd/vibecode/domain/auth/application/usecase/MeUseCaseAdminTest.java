package com.yd.vibecode.domain.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.domain.exam.domain.service.ExamService;
import com.yd.vibecode.domain.auth.domain.service.UserService;
import com.yd.vibecode.global.security.TokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MeUseCaseAdminTest {

    @InjectMocks
    private MeUseCase meUseCase;

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private UserService userService;
    @Mock
    private AdminService adminService;
    @Mock
    private ExamParticipantService examParticipantService;
    @Mock
    private ExamService examService;

    @Test
    @DisplayName("ADMIN 내 정보 조회 - displayName·adminNumber 분리")
    void me_admin_returns_display_name_and_admin_number() {
        String token = "accessToken";
        Admin admin = Admin.builder()
                .adminNumber("ADM-001")
                .displayName("김관리")
                .email("admin@example.com")
                .passwordHash("hash")
                .role(AdminRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(admin, "id", 1L);

        given(tokenProvider.getId(token)).willReturn(Optional.of("1"));
        given(tokenProvider.getRole(token)).willReturn(Optional.of("ADMIN"));
        given(adminService.findById(1L)).willReturn(admin);

        MeResponse response = meUseCase.execute(token);

        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.participant().name()).isEqualTo("김관리");
        assertThat(response.participant().displayName()).isEqualTo("김관리");
        assertThat(response.participant().phone()).isEqualTo("admin@example.com");
        assertThat(response.participant().adminNumber()).isEqualTo("ADM-001");
    }

    @Test
    @DisplayName("ADMIN 내 정보 조회 - displayName 없으면 adminNumber fallback")
    void me_admin_fallback_display_name() {
        String token = "accessToken";
        Admin admin = Admin.builder()
                .adminNumber("ADM-002")
                .email("legacy@example.com")
                .passwordHash("hash")
                .role(AdminRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(admin, "id", 2L);

        given(tokenProvider.getId(token)).willReturn(Optional.of("2"));
        given(tokenProvider.getRole(token)).willReturn(Optional.of("ADMIN"));
        given(adminService.findById(2L)).willReturn(admin);

        MeResponse response = meUseCase.execute(token);

        assertThat(response.participant().name()).isEqualTo("ADM-002");
        assertThat(response.participant().displayName()).isNull();
        assertThat(response.participant().adminNumber()).isEqualTo("ADM-002");
    }
}

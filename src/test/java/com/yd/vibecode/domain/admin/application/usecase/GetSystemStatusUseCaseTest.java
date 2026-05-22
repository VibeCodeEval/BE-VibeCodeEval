package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yd.vibecode.domain.admin.application.dto.response.SystemStatusResponse;

@ExtendWith(MockitoExtension.class)
class GetSystemStatusUseCaseTest {

    @Test
    @DisplayName("DB ping 성공 시 database 서비스 UP")
    void execute_databaseUp_whenSelect1Succeeds() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        given(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).willReturn(1);

        GetSystemStatusUseCase useCase = new GetSystemStatusUseCase(jdbcTemplate, "http://localhost:1");

        SystemStatusResponse response = useCase.execute();

        assertThat(response.services()).hasSize(3);
        assertThat(response.services().get(0).key()).isEqualTo("api");
        assertThat(response.services().get(0).status()).isEqualTo("UP");
        assertThat(response.services().get(1).key()).isEqualTo("database");
        assertThat(response.services().get(1).status()).isEqualTo("UP");
        assertThat(response.services().get(1).latencyMs()).isNotNull();
    }

    @Test
    @DisplayName("DB ping 실패 시 database 서비스 DOWN")
    void execute_databaseDown_whenSelect1Fails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        given(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
            .willThrow(new RuntimeException("connection refused"));

        GetSystemStatusUseCase useCase = new GetSystemStatusUseCase(jdbcTemplate, "http://localhost:1");

        SystemStatusResponse response = useCase.execute();

        assertThat(response.services().get(1).status()).isEqualTo("DOWN");
    }
}

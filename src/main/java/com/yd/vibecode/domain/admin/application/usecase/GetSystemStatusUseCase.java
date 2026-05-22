package com.yd.vibecode.domain.admin.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.yd.vibecode.domain.admin.application.dto.response.SystemStatusResponse;
import com.yd.vibecode.domain.admin.application.dto.response.SystemStatusResponse.ServiceStatusItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GetSystemStatusUseCase {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";

    private static final ParameterizedTypeReference<Map<String, Object>> AI_HEALTH_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate healthRestTemplate;
    private final String aiServerUrl;

    public GetSystemStatusUseCase(
            JdbcTemplate jdbcTemplate,
            @Value("${ai.server.url}") String aiServerUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.aiServerUrl = normalizeBaseUrl(aiServerUrl);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(5_000);
        this.healthRestTemplate = new RestTemplate(factory);
    }

    public SystemStatusResponse execute() {
        List<ServiceStatusItem> services = new ArrayList<>();
        services.add(checkApiService());
        services.add(checkDatabaseService());
        services.add(checkAiGatewayService());
        return new SystemStatusResponse(services);
    }

    private ServiceStatusItem checkApiService() {
        return new ServiceStatusItem("api", "API 서버", STATUS_UP, null);
    }

    private ServiceStatusItem checkDatabaseService() {
        long start = System.nanoTime();
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            if (result != null && result == 1) {
                return new ServiceStatusItem("database", "데이터베이스", STATUS_UP, latencyMs);
            }
            log.warn("[SystemStatus] Database ping returned unexpected result: {}", result);
        } catch (Exception e) {
            log.error("[SystemStatus] Database health check failed", e);
        }
        Long latencyMs = elapsedMsOrNull(start);
        return new ServiceStatusItem("database", "데이터베이스", STATUS_DOWN, latencyMs);
    }

    private ServiceStatusItem checkAiGatewayService() {
        long start = System.nanoTime();
        String healthUrl = aiServerUrl + "/health";
        try {
            ResponseEntity<Map<String, Object>> response = healthRestTemplate.exchange(
                    healthUrl,
                    HttpMethod.GET,
                    null,
                    AI_HEALTH_RESPONSE_TYPE);
            long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            if (response.getStatusCode().is2xxSuccessful() && isAiHealthOk(response.getBody())) {
                return new ServiceStatusItem("ai", "AI 게이트웨이", STATUS_UP, latencyMs);
            }
            log.warn("[SystemStatus] AI health check non-success: status={}, body={}",
                    response.getStatusCode(), response.getBody());
        } catch (RestClientException e) {
            log.error("[SystemStatus] AI health check failed: url={}", healthUrl, e);
        } catch (Exception e) {
            log.error("[SystemStatus] AI health check unexpected error: url={}", healthUrl, e);
        }
        Long latencyMs = elapsedMsOrNull(start);
        return new ServiceStatusItem("ai", "AI 게이트웨이", STATUS_DOWN, latencyMs);
    }

    private static boolean isAiHealthOk(Map<String, Object> body) {
        if (body == null) {
            return false;
        }
        Object status = body.get("status");
        if (status == null) {
            return true;
        }
        String normalized = status.toString().trim().toLowerCase();
        return "ok".equals(normalized) || "degraded".equals(normalized);
    }

    private static Long elapsedMsOrNull(long startNano) {
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
        return ms >= 0 ? ms : null;
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8000";
        }
        String trimmed = url.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}

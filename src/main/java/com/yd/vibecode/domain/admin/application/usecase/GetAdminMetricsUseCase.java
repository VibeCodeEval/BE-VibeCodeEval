package com.yd.vibecode.domain.admin.application.usecase;

import com.yd.vibecode.domain.admin.application.dto.response.AdminMetricsResponse;
import org.springframework.stereotype.Service;

@Service
public class GetAdminMetricsUseCase {

    public AdminMetricsResponse execute(Long examId) {
        // Mock data - will be replaced when statistics domain is implemented
        return new AdminMetricsResponse(
            new AdminMetricsResponse.ConcurrencyMetrics(0, 0),
            new AdminMetricsResponse.QueueMetrics(0, 0.0),
            new AdminMetricsResponse.ErrorMetrics(0.0, "NONE")
        );
    }
}

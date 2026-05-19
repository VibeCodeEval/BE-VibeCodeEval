package com.yd.vibecode.domain.admin.application.dto.response;

import java.util.List;

public record SystemStatusResponse(
    List<ServiceStatusItem> services
) {
    public record ServiceStatusItem(
        String key,
        String name,
        String status,
        Long latencyMs
    ) {}
}

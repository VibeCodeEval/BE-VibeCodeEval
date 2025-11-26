package com.yd.vibecode.domain.admin.application.dto.response;

public record AdminMetricsResponse(
    ConcurrencyMetrics concurrency,
    QueueMetrics queue,
    ErrorMetrics errors
) {
    public record ConcurrencyMetrics(
        int activeExaminees,
        int wsConnections
    ) {}

    public record QueueMetrics(
        int judgeQueueDepth,
        double avgWaitSec
    ) {}

    public record ErrorMetrics(
        double rate1m,
        String last
    ) {}
}

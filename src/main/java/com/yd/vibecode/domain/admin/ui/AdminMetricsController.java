package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.response.AdminMetricsResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetAdminMetricsUseCase;
import com.yd.vibecode.global.swagger.AdminMetricsApi;
import com.yd.vibecode.global.common.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/metrics")
public class AdminMetricsController implements AdminMetricsApi {

    private final GetAdminMetricsUseCase getAdminMetricsUseCase;

    @GetMapping
    @Override
    public BaseResponse<AdminMetricsResponse> getMetrics(@RequestParam Long examId) {
        AdminMetricsResponse response = getAdminMetricsUseCase.execute(examId);
        return BaseResponse.onSuccess(response);
    }
}

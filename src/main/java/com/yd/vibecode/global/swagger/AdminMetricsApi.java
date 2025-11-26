package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.response.AdminMetricsResponse;
import com.yd.vibecode.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "운영 메트릭", description = "시험 운영 메트릭 조회 API")
public interface AdminMetricsApi extends BaseApi {

    @Operation(summary = "시험 운영 메트릭 조회", description = "동시 접속, 큐 상태, 에러율 등 운영 지표를 반환합니다.")
    BaseResponse<AdminMetricsResponse> getMetrics(Long examId);
}



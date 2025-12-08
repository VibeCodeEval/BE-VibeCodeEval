package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.statistics.application.dto.response.ExamStatisticResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "통계", description = "시험 통계 조회 API")
public interface StatisticsApi extends BaseApi {

    @Operation(
            summary = "최신 통계 조회",
            description = "시험의 최신 통계 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ExamStatisticResponse.class))
    )
    BaseResponse<ExamStatisticResponse> getLatestStatistics(
            @Parameter(description = "시험 ID", required = true, example = "1")
            Long examId
    );
}


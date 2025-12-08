package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.exam.application.dto.response.ExamStateResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "시험 관리", description = "시험 상태 조회 및 관리 API")
public interface ExamApi extends BaseApi {

    @Operation(
            summary = "시험 상태 조회",
            description = "시험의 현재 상태(WAITING/RUNNING/ENDED), 시작/종료 시각, 서버 시각을 조회합니다. " +
                    "클라이언트 타이머 동기화에 사용됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ExamStateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<ExamStateResponse> getExamState(Long examId);

}


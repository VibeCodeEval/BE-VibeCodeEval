package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.problem.application.dto.response.AssignmentResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "문제 관리", description = "배정 문제 및 스펙 조회 API")
public interface ProblemApi extends BaseApi {

    @Operation(
            summary = "배정 문제 조회",
            description = "시험에 배정된 문제와 해당 문제의 스펙 정보를 조회합니다. " +
                    "세션 시작 시 spec_id/spec.version이 잠금되며, 대화/채점/평가 모두 동일 스펙을 참조합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AssignmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (시험에 참여하지 않음, 세션이 시작되지 않음 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시험 또는 배정 문제를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<AssignmentResponse> getAssignment(Long examId, Long userId);
}


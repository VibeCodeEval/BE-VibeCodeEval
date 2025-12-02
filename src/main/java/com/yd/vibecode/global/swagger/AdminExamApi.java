package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.exam.application.dto.request.CreateExamRequest;
import com.yd.vibecode.domain.exam.application.dto.request.ExtendExamRequest;
import com.yd.vibecode.domain.exam.application.dto.response.ExamResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "시험 제어 (관리자)", description = "시험 시작/종료/연장 관리 API")
public interface AdminExamApi extends BaseApi {

    @Operation(
            summary = "시험 생성",
            description = "새로운 시험을 생성합니다. 제목, 시작/종료 시각이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시험 생성 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 누락 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<ExamResponse> createExam(@Parameter(hidden = true) String adminId, @RequestBody CreateExamRequest request);

    @Operation(
            summary = "시험 목록 조회",
            description = "등록된 모든 시험의 목록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class))
    )
    BaseResponse<List<ExamResponse>> getExams();

    @Operation(
            summary = "시험 시작",
            description = "시험 상태를 WAITING에서 RUNNING으로 변경합니다. " +
                    "시작 시각을 현재 시각으로 설정하고, 관련 참가자들에게 상태 변경을 알립니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시험 시작 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 시작된 시험 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<Void> startExam(Long id);

    @Operation(
            summary = "시험 종료",
            description = "시험 상태를 RUNNING에서 ENDED로 변경합니다. " +
                    "종료 시각을 현재 시각으로 설정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시험 종료 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 종료된 시험 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<Void> endExam(Long id);

    @Operation(
            summary = "시험 시간 연장",
            description = "시험 종료 시각을 지정한 분만큼 연장합니다. " +
                    "최소 1분 이상이어야 하며, 시험 상태가 RUNNING일 때만 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시험 시간 연장 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (연장 시간이 1분 미만, 시험 상태가 RUNNING이 아님 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<Void> extendExam(Long id, ExtendExamRequest request);

    @Operation(
            summary = "시험 삭제",
            description = "시험을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시험 삭제 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<Void> deleteExam(Long id);
}


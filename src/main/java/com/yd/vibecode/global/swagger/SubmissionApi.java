package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "제출 관리", description = "코드 제출 및 제출 결과 조회 API")
public interface SubmissionApi extends BaseApi {

    @Operation(
            summary = "코드 제출",
            description = "시험에 코드를 제출합니다. 제출은 비동기로 처리되며, " +
                    "202 Accepted 응답과 함께 제출 ID와 초기 상태(QUEUED)를 반환합니다. " +
                    "채점은 백그라운드에서 진행되며, 결과는 제출 상세 조회 API를 통해 주기적으로 확인할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "제출 접수 완료 (비동기 처리 시작)",
                    content = @Content(schema = @Schema(implementation = SubmitResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (언어/코드 필수값 누락, 허용되지 않은 언어 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<SubmitResponse> submit(Long examId, Long userId, SubmitRequest request);

    @Operation(
            summary = "제출 상세 조회",
            description = "제출 ID로 제출 상세 정보를 조회합니다. " +
                    "채점 완료 시 테스트 케이스 결과, 점수, 메트릭 정보를 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SubmissionDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "제출을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<SubmissionDetailResponse> getSubmissionDetail(Long submissionId);
}


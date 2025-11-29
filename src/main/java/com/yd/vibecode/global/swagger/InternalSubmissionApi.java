package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "내부 채점 결과 수신", description = "FastAPI에서 채점 완료 후 전송하는 내부 API")
public interface InternalSubmissionApi extends BaseApi {

    @Operation(
            summary = "채점 결과 수신",
            description = "FastAPI에서 채점 완료 후 전송하는 내부 API입니다. " +
                    "⚠️ 주의: 이 API는 내부 네트워크에서만 호출되어야 합니다. " +
                    "프로덕션에서는 IP 화이트리스트 또는 내부 토큰으로 보호 필요합니다.",
            hidden = true
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "채점 결과 수신 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (요청 바디 검증 실패 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "제출을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<Void> receiveScoringResult(Long id, ScoringResultRequest request);
}


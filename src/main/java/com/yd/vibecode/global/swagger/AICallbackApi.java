package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.submission.application.dto.request.AISubmissionStatusRequest;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "AI 콜백", description = "AI 제출 상태 업데이트 수신 API")
public interface AICallbackApi extends BaseApi {

    @Operation(
            summary = "제출 상태 업데이트",
            description = "AI 서버로부터 제출 평가 완료 상태를 수신하여 Submission 상태를 업데이트합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "상태 업데이트 성공"
    )
    @ApiResponse(
            responseCode = "404",
            description = "제출을 찾을 수 없음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 상태 값",
            content = @Content
    )
    BaseResponse<Void> receiveAnalysisResult(AISubmissionStatusRequest request);
}

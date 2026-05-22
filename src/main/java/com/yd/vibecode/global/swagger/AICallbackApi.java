package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.submission.application.dto.request.AISubmissionStatusRequest;
import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "AI 콜백", description = "AI Worker 콜백 API (상태 알림 / 채점 결과)")
public interface AICallbackApi extends BaseApi {

    @Operation(
            summary = "제출 상태 업데이트",
            description = "AI 서버로부터 제출 상태만 수신합니다 (RUNNING, FAILED 권장). "
                    + "채점 완료(DONE) 및 testCases/score는 /submissions/{submissionId}/result 로 전달하세요."
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

    @Operation(
            summary = "채점 결과 수신",
            description = "AI Worker가 N9 이후 testCases, score, status를 전송합니다. "
                    + "submission_runs, scores 저장 및 SSE(case_result, scoring_complete, final_score) 발행. "
                    + "재호출 시 기존 runs/score를 덮어씁니다."
    )
    @ApiResponse(responseCode = "200", description = "채점 결과 수신 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    @ApiResponse(responseCode = "404", description = "제출을 찾을 수 없음", content = @Content)
    BaseResponse<Void> receiveScoringResult(Long submissionId, ScoringResultRequest request);
}

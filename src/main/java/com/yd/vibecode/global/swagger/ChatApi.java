package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.request.UpdateTokenUsageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.ChatHistoryResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "채팅", description = "대화 저장 및 조회, 토큰 사용량 업데이트 API")
public interface ChatApi extends BaseApi {

    @Operation(
            summary = "채팅 메시지 저장",
            description = "대화 메시지를 저장합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "저장 성공"
    )
    BaseResponse<Void> saveChatMessage(SaveChatMessageRequest request);

    @Operation(
            summary = "채팅 히스토리 조회",
            description = "시험 및 참가자 ID로 채팅 히스토리를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ChatHistoryResponse.class))
    )
    BaseResponse<ChatHistoryResponse> getChatHistory(
            @Parameter(description = "시험 ID", required = true, example = "1")
            Long examId,
            @Parameter(description = "참가자 ID", required = true, example = "1")
            Long participantId
    );

    @Operation(
            summary = "토큰 사용량 업데이트",
            description = "AI 콜백을 통해 토큰 사용량을 업데이트합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "업데이트 성공"
    )
    BaseResponse<Void> updateTokenUsage(UpdateTokenUsageRequest request);
}


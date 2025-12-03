package com.yd.vibecode.global.swagger;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "실시간 채점 스트리밍 (관리자)", description = "SSE를 통한 실시간 채점 결과 스트리밍 API")
public interface SseSubmissionApi {

    @Operation(
            summary = "실시간 채점 결과 스트리밍",
            description = "제출 ID에 대한 채점 결과를 SSE(Server-Sent Events)로 실시간 스트리밍합니다. " +
                    "관리자/마스터 권한이 필요하며, 채점 진행 상황과 최종 점수를 실시간으로 수신할 수 있습니다. " +
                    "연결 타임아웃은 30분입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "SSE 연결 성공 (text/event-stream)",
                    content = @Content(mediaType = "text/event-stream", schema = @Schema(implementation = SseEmitter.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "제출을 찾을 수 없음"
            )
    })
    SseEmitter streamScoringResult(Long submissionId);
}


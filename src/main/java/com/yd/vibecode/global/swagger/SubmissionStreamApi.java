package com.yd.vibecode.global.swagger;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "제출 채점 스트림 (응시자)", description = "본인 제출에 대한 채점 SSE (로그인 응시자 전용)")
public interface SubmissionStreamApi extends BaseApi {

    @Operation(
            summary = "응시자 채점 결과 SSE",
            description = """
                    제출 ID에 대한 채점 진행 상황을 SSE로 수신합니다. **해당 제출의 소유자(토큰의 user id = submissions.participant_id)**만 연결할 수 있습니다.

                    이벤트는 관리자 스트림과 동일하게 `case_result`, `scoring_complete`, `final_score`(선택) 순으로 전달되며, 제출 코드·루브릭 등 민감 필드는 포함하지 않습니다.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공", content = @Content()),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content()),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 제출", content = @Content()),
            @ApiResponse(responseCode = "404", description = "제출 없음", content = @Content())
    })
    SseEmitter streamScoringResultForExaminee(
            @Parameter(description = "제출 ID", example = "1") Long submissionId,
            @Parameter(hidden = true) Long currentUserId);
}

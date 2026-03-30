package com.yd.vibecode.global.swagger;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "채점 스트리밍 (관리자)", description = "채점 결과 SSE 실시간 스트리밍 API")
public interface AdminSubmissionStreamApi extends BaseApi {

    @Operation(
        summary = "채점 결과 SSE 스트리밍",
        description = """
            제출 ID에 대한 채점 결과를 SSE(Server-Sent Events)로 실시간 스트리밍합니다.
            Admin/Master 권한이 필요합니다.

            **이벤트 타입:**
            - `case_result`: 테스트 케이스별 결과 (caseIndex, verdict, timeMs, memKb)
            - `final_score`: 최종 점수 (promptScore, perfScore, correctnessScore, total)

            **주의:** Swagger UI에서는 SSE를 직접 테스트하기 어렵습니다.
            curl 또는 브라우저 개발자 도구를 사용하세요.

            curl 예시:
            ```
            curl -N -H "Authorization: Bearer {token}" http://localhost:8080/api/admin/submissions/{id}/stream
            ```
            """
    )
    @ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공")
    SseEmitter streamScoringResult(
        @Parameter(description = "제출 ID", example = "88001") Long submissionId
    );
}

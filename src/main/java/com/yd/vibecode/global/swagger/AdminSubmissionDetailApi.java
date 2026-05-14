package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.submission.application.dto.response.AdminSubmissionDetailResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 제출 상세", description = "제출 코드·루브릭 등 민감 정보 포함 (ADMIN/MASTER 전용)")
public interface AdminSubmissionDetailApi extends BaseApi {

    @Operation(
            summary = "관리자 제출 상세 조회",
            description = """
                    제출 ID로 상세 정보를 조회합니다. **ADMIN 또는 MASTER** 권한이 필요합니다.

                    일반 `GET /api/submissions/{id}`와 달리 **제출 소스 코드(`codeInline`)** 및
                    **채점 루브릭 JSON(`rubricJson`)**을 포함합니다. 쿠키 기반 인증을 사용합니다.
                    """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AdminSubmissionDetailResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "제출을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    BaseResponse<AdminSubmissionDetailResponse> getAdminSubmissionDetail(
            @Parameter(description = "제출 ID", example = "1") Long submissionId);
}

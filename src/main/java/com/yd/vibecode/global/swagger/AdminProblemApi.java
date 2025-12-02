package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.request.CreateProblemRequest;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemSpecResponse;
import com.yd.vibecode.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "문제 관리 (관리자)", description = "문제 생성/조회/삭제 및 스펙 관리 API")
public interface AdminProblemApi extends BaseApi {

    @Operation(summary = "문제 생성", description = "새로운 문제를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    BaseResponse<ProblemResponse> createProblem(CreateProblemRequest request);

    @Operation(summary = "문제 목록 조회", description = "전체 문제 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    BaseResponse<List<ProblemResponse>> getProblems();

    @Operation(summary = "문제 삭제", description = "문제를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    BaseResponse<Void> deleteProblem(Long problemId);

    @Operation(summary = "문제 스펙 조회", description = "특정 문제의 스펙 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    BaseResponse<List<ProblemSpecResponse>> getProblemSpecs(Long problemId);
}

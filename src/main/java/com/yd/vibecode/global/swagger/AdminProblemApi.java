package com.yd.vibecode.global.swagger;

import java.util.List;

import com.yd.vibecode.domain.admin.application.dto.request.CreateProblemRequest;
import com.yd.vibecode.domain.admin.application.dto.request.UpdateProblemAvailabilityRequest;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemDetailResponse;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemSpecResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "문제 관리 (관리자)", description = "문제 생성/조회/삭제 및 스펙 관리 API")
public interface AdminProblemApi extends BaseApi {

    @Operation(summary = "문제 생성", description = "새로운 문제를 DRAFT 상태로 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    BaseResponse<ProblemResponse> createProblem(
            @RequestBody(description = "문제 생성 요청", required = true) CreateProblemRequest request);

    @Operation(summary = "문제 목록 조회", description = "전체 문제 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    BaseResponse<List<ProblemResponse>> getProblems();

    @Operation(
            summary = "문제 사용 가능 여부 변경",
            description = "available=true이면 PUBLISHED, false이면 ARCHIVED로 변경합니다. "
                    + "시험 생성 시 PUBLISHED 문제만 랜덤 배정 후보가 됩니다."
    )
    @ApiResponse(responseCode = "200", description = "변경 성공")
    BaseResponse<ProblemResponse> updateProblemAvailability(
            Long problemId,
            @RequestBody(description = "사용 가능 여부", required = true) UpdateProblemAvailabilityRequest request);

    @Operation(summary = "문제 삭제", description = "문제를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    BaseResponse<Void> deleteProblem(Long problemId);

    @Operation(summary = "문제 스펙 조회", description = "특정 문제의 스펙 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    BaseResponse<List<ProblemSpecResponse>> getProblemSpecs(Long problemId);

    @Operation(
            summary = "문제 상세 조회",
            description = "problemId 기준으로 공개 가능한 문제 본문(contentMd) 및 스펙 메타를 조회합니다. "
                    + "테스트케이스·정답 데이터는 포함하지 않습니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    BaseResponse<ProblemDetailResponse> getProblemDetail(Long problemId);
}

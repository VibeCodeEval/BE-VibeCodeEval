package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.response.ProblemSpecResponse;
import com.yd.vibecode.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "문제 스펙", description = "문제 스펙 조회 API")
public interface AdminProblemApi extends BaseApi {

    @Operation(summary = "문제 스펙 목록 조회", description = "선택한 문제의 모든 버전 스펙을 반환합니다.")
    BaseResponse<List<ProblemSpecResponse>> getProblemSpecs(Long problemId);
}



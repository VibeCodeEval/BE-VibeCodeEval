package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberIssueRequest;
import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberUpdateRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 번호", description = "관리자 번호 발급 및 수정 API")
public interface AdminNumberApi extends BaseApi {

    @Operation(summary = "관리자 번호 발급", description = "MASTER 계정이 신규 관리자 번호를 발급합니다.")
    BaseResponse<AdminNumberResponse> issueAdminNumber(
            @Parameter(hidden = true) String adminId,
            AdminNumberIssueRequest request);

    @Operation(summary = "관리자 번호 수정", description = "관리자 번호의 라벨, 만료일, 활성 상태를 변경합니다.")
    BaseResponse<AdminNumberResponse> updateAdminNumber(
            @Parameter(hidden = true) String adminId,
            String adminNumber,
            AdminNumberUpdateRequest request);
}



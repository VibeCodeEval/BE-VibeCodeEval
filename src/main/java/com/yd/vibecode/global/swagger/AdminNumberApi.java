package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberIssueRequest;
import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberUpdateRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminListResponse;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 번호", description = "관리자 번호 발급 및 수정 API")
public interface AdminNumberApi extends BaseApi {

    @Operation(
            summary = "모든 관리자 조회",
            description = "MASTER 계정만 사용 가능합니다. 등록된 모든 관리자 목록을 조회합니다. " +
                    "관리자 번호 수정 시 필요한 adminNumber를 확인할 수 있습니다."
    )
    BaseResponse<AdminListResponse> getAllAdmins(@Parameter(hidden = true) String adminId);

    @Operation(summary = "관리자 번호 발급", description = "MASTER 계정이 신규 관리자 번호를 발급합니다.")
    BaseResponse<AdminNumberResponse> issueAdminNumber(
            @Parameter(hidden = true) String adminId,
            AdminNumberIssueRequest request);

    @Operation(summary = "관리자 번호 상태 변경", description = "관리자 번호의 라벨, 만료일, 활성 상태를 변경합니다. 관리자 번호 값 자체는 변경되지 않습니다.")
    BaseResponse<AdminNumberResponse> updateAdminNumber(
            @Parameter(hidden = true) String adminId,
            String adminNumber,
            AdminNumberUpdateRequest request);
}



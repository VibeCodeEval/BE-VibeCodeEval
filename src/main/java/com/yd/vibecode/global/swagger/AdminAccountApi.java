package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.request.ChangeAdminPasswordRequest;
import com.yd.vibecode.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 계정", description = "관리자 계정 관리 API")
public interface AdminAccountApi extends BaseApi {

    @Operation(summary = "관리자 비밀번호 변경", description = "현재 로그인한 관리자의 비밀번호를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "변경 완료", content = @Content())
    BaseResponse<Void> changePassword(String adminId, ChangeAdminPasswordRequest request);
}



package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.response.AdminActivityLogPageResponse;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 활동 로그", description = "관리자 본인 활동 로그 조회 API")
public interface AdminActivityLogApi extends BaseApi {

    @Operation(
            summary = "관리자 활동 로그 조회",
            description = "현재 로그인한 관리자 본인의 활동 로그를 최신순으로 조회합니다. "
                    + "대시보드 최근 활동은 size=5 등으로 동일 API를 사용할 수 있습니다.")
    BaseResponse<AdminActivityLogPageResponse> getLogs(
            Long adminId,
            String keyword,
            AdminActivityLogType type,
            int page,
            int size);
}

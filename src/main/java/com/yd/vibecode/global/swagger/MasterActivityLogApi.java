package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.response.MasterActivityLogPageResponse;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "마스터 활동 로그", description = "MASTER 전용 플랫폼 활동 로그 조회 API")
public interface MasterActivityLogApi extends BaseApi {

    @Operation(
            summary = "마스터 활동 로그 조회 (MASTER 전용)",
            description = "MASTER 권한 사용자만 조회할 수 있습니다. "
                    + "관리자 가입 번호 발급·비활성화·재활성화, 관리자 가입 완료, 계정 삭제, 비밀번호 재설정 이력을 최신순으로 조회합니다. "
                    + "type: ADMIN_SIGNUP_CODE_ISSUED, ADMIN_SIGNUP_CODE_DEACTIVATED, ADMIN_SIGNUP_CODE_REACTIVATED, "
                    + "ADMIN_SIGNED_UP, ADMIN_ACCOUNT_DELETED, ADMIN_PASSWORD_RESET. "
                    + "응답에는 가입 번호 원문, 비밀번호, 토큰이 포함되지 않습니다.")
    BaseResponse<MasterActivityLogPageResponse> getLogs(
            Long adminId,
            String keyword,
            MasterActivityLogType type,
            int page,
            int size);
}

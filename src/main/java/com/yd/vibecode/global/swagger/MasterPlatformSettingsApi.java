package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateMasterPlatformSettingsRequest;
import com.yd.vibecode.domain.admin.application.dto.response.MasterPlatformSettingsResponse;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "마스터 플랫폼 설정", description = "MASTER 전용 플랫폼 전역 설정 API")
public interface MasterPlatformSettingsApi extends BaseApi {

    @Operation(
            summary = "플랫폼 전역 설정 조회 (MASTER 전용)",
            description = "로그/제출물 보관 기간, 만료 데이터 자동 삭제 정책을 조회합니다. "
                    + "설정 row가 없으면 기본값으로 생성 후 반환합니다.")
    BaseResponse<MasterPlatformSettingsResponse> getSettings(Long adminId);

    @Operation(
            summary = "플랫폼 전역 설정 수정 (MASTER 전용)",
            description = "플랫폼 전역 설정을 수정합니다. autoDeleteExpiredData=false이면 "
                    + "향후 만료 데이터 자동 삭제 스케줄러는 실행되지 않아야 하는 정책 값입니다. "
                    + "이번 단계에서는 실제 삭제 스케줄러는 구현하지 않습니다.")
    BaseResponse<MasterPlatformSettingsResponse> updateSettings(
            Long adminId,
            UpdateMasterPlatformSettingsRequest request);
}

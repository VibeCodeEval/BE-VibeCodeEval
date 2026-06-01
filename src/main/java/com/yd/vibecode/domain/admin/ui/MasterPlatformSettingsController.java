package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateMasterPlatformSettingsRequest;
import com.yd.vibecode.domain.admin.application.dto.response.MasterPlatformSettingsResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetMasterPlatformSettingsUseCase;
import com.yd.vibecode.domain.admin.application.usecase.UpdateMasterPlatformSettingsUseCase;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.MasterPlatformSettingsApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/master/settings")
public class MasterPlatformSettingsController implements MasterPlatformSettingsApi {

    private final GetMasterPlatformSettingsUseCase getMasterPlatformSettingsUseCase;
    private final UpdateMasterPlatformSettingsUseCase updateMasterPlatformSettingsUseCase;

    @GetMapping
    @Override
    public BaseResponse<MasterPlatformSettingsResponse> getSettings(@CurrentUser Long adminId) {
        return BaseResponse.onSuccess(getMasterPlatformSettingsUseCase.execute(adminId));
    }

    @PutMapping
    @Override
    public BaseResponse<MasterPlatformSettingsResponse> updateSettings(
            @CurrentUser Long adminId,
            @Valid @RequestBody UpdateMasterPlatformSettingsRequest request) {
        return BaseResponse.onSuccess(updateMasterPlatformSettingsUseCase.execute(adminId, request));
    }
}

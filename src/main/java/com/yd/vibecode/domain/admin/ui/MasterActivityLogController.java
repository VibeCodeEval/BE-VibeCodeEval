package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.response.MasterActivityLogPageResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetMasterActivityLogsUseCase;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.MasterActivityLogApi;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/master/logs")
public class MasterActivityLogController implements MasterActivityLogApi {

    private final GetMasterActivityLogsUseCase getMasterActivityLogsUseCase;

    @GetMapping
    @Override
    public BaseResponse<MasterActivityLogPageResponse> getLogs(
            @CurrentUser Long adminId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MasterActivityLogType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        MasterActivityLogPageResponse response = getMasterActivityLogsUseCase.execute(
                adminId, keyword, type, page, size);
        return BaseResponse.onSuccess(response);
    }
}

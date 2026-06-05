package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.response.AdminActivityLogPageResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetAdminActivityLogsUseCase;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.AdminActivityLogApi;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/logs")
public class AdminActivityLogController implements AdminActivityLogApi {

    private final GetAdminActivityLogsUseCase getAdminActivityLogsUseCase;

    @GetMapping
    @Override
    public BaseResponse<AdminActivityLogPageResponse> getLogs(
            @CurrentUser Long adminId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AdminActivityLogType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminActivityLogPageResponse response = getAdminActivityLogsUseCase.execute(
                adminId, keyword, type, page, size);
        return BaseResponse.onSuccess(response);
    }
}

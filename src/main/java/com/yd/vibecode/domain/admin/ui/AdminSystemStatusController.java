package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.response.SystemStatusResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetSystemStatusUseCase;
import com.yd.vibecode.global.common.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/system-status")
public class AdminSystemStatusController {

    private final GetSystemStatusUseCase getSystemStatusUseCase;

    @GetMapping
    public BaseResponse<SystemStatusResponse> getSystemStatus() {
        SystemStatusResponse response = getSystemStatusUseCase.execute();
        return BaseResponse.onSuccess(response);
    }
}

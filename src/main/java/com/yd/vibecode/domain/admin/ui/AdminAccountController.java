package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.request.ChangeAdminPasswordRequest;
import com.yd.vibecode.domain.admin.application.usecase.ChangeAdminPasswordUseCase;
import com.yd.vibecode.global.swagger.AdminAccountApi;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/account")
public class AdminAccountController implements AdminAccountApi {

    private final ChangeAdminPasswordUseCase changeAdminPasswordUseCase;

    @PatchMapping("/password")
    @Override
    public BaseResponse<Void> changePassword(
        @CurrentUser String adminId,
        @Valid @RequestBody ChangeAdminPasswordRequest request
    ) {
        changeAdminPasswordUseCase.execute(Long.parseLong(adminId), request);
        return BaseResponse.onSuccess();
    }
}

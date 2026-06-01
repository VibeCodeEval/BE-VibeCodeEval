package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.request.ChangeAdminPasswordRequest;
import com.yd.vibecode.domain.admin.application.usecase.ChangeAdminPasswordUseCase;
import com.yd.vibecode.domain.admin.application.usecase.DeleteOwnAdminAccountUseCase;
import com.yd.vibecode.global.swagger.AdminAccountApi;
import com.yd.vibecode.global.annotation.AccessToken;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/account")
public class AdminAccountController implements AdminAccountApi {

    private final ChangeAdminPasswordUseCase changeAdminPasswordUseCase;
    private final DeleteOwnAdminAccountUseCase deleteOwnAdminAccountUseCase;

    @PatchMapping("/password")
    @Override
    public BaseResponse<Void> changePassword(
        @CurrentUser String adminId,
        @Valid @RequestBody ChangeAdminPasswordRequest request
    ) {
        changeAdminPasswordUseCase.execute(Long.parseLong(adminId), request);
        return BaseResponse.onSuccess();
    }

    @DeleteMapping
    @Operation(
            summary = "본인 관리자 계정 삭제",
            description = "현재 로그인한 관리자가 자신의 계정을 soft delete 합니다. 마스터 계정은 삭제할 수 없습니다."
    )
    public BaseResponse<Void> deleteOwnAccount(
        @CurrentUser String adminId,
        @AccessToken String accessToken,
        HttpServletResponse httpResponse
    ) {
        deleteOwnAdminAccountUseCase.execute(Long.parseLong(adminId), accessToken, httpResponse);
        return BaseResponse.onSuccess();
    }
}

package com.yd.vibecode.domain.admin.ui;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberIssueRequest;
import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberUpdateRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.domain.admin.application.usecase.IssueAdminNumberUseCase;
import com.yd.vibecode.domain.admin.application.usecase.UpdateAdminNumberUseCase;
import com.yd.vibecode.global.swagger.AdminNumberApi;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin-numbers")
public class AdminNumberController implements AdminNumberApi {

    private final IssueAdminNumberUseCase issueAdminNumberUseCase;
    private final UpdateAdminNumberUseCase updateAdminNumberUseCase;

    @PostMapping
    @Override
    public BaseResponse<AdminNumberResponse> issueAdminNumber(@CurrentUser String adminId,
                                                              @Valid @RequestBody AdminNumberIssueRequest request) {
        AdminNumberResponse response = issueAdminNumberUseCase.execute(Long.parseLong(adminId), request);
        return BaseResponse.onSuccess(response);
    }

    @PatchMapping("/{adminNumber}")
    @Override
    public BaseResponse<AdminNumberResponse> updateAdminNumber(@CurrentUser String adminId,
                                                               @PathVariable String adminNumber,
                                                               @Valid @RequestBody AdminNumberUpdateRequest request) {
        AdminNumberResponse response = updateAdminNumberUseCase.execute(Long.parseLong(adminId), adminNumber, request);
        return BaseResponse.onSuccess(response);
    }
}



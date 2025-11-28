package com.yd.vibecode.domain.admin.ui;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.request.CreateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.request.UpdateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.admin.application.usecase.CreateEntryCodeUseCase;
import com.yd.vibecode.domain.admin.application.usecase.UpdateEntryCodeUseCase;
import com.yd.vibecode.global.swagger.AdminEntryCodeApi;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/entry-codes")
public class AdminEntryCodeController implements AdminEntryCodeApi {

    private final CreateEntryCodeUseCase createEntryCodeUseCase;
    private final UpdateEntryCodeUseCase updateEntryCodeUseCase;

    @PostMapping
    @Override
    public BaseResponse<EntryCodeResponse> createEntryCode(
        @CurrentUser String adminId,
        @Valid @RequestBody CreateEntryCodeRequest createRequest
    ) {
        EntryCodeResponse response = createEntryCodeUseCase.execute(Long.parseLong(adminId), createRequest);
        return BaseResponse.onSuccess(response);
    }

    @PatchMapping("/{code}")
    @Override
    public BaseResponse<EntryCodeResponse> updateEntryCode(
        @PathVariable String code,
        @Valid @RequestBody UpdateEntryCodeRequest updateRequest
    ) {
        EntryCodeResponse response = updateEntryCodeUseCase.execute(code, updateRequest);
        return BaseResponse.onSuccess(response);
    }
}

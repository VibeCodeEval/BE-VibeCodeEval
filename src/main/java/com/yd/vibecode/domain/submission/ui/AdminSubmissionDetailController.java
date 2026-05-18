package com.yd.vibecode.domain.submission.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.submission.application.dto.response.AdminSubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.usecase.GetAdminSubmissionDetailUseCase;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.AdminSubmissionDetailApi;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 제출 상세 (민감 필드 포함).
 * <p>
 * 보안: {@code /api/admin/**} 는 SecurityConfig 에서 ADMIN/MASTER 만 허용.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/submissions")
public class AdminSubmissionDetailController implements AdminSubmissionDetailApi {

    private final GetAdminSubmissionDetailUseCase getAdminSubmissionDetailUseCase;

    @Override
    @GetMapping("/{submissionId}")
    public BaseResponse<AdminSubmissionDetailResponse> getAdminSubmissionDetail(
            @PathVariable Long submissionId,
            @CurrentUser Long adminUserId) {
        AdminSubmissionDetailResponse response =
                getAdminSubmissionDetailUseCase.execute(adminUserId, submissionId);
        return BaseResponse.onSuccess(response);
    }
}

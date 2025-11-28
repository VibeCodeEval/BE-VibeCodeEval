package com.yd.vibecode.domain.admin.ui;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.response.ProblemSpecResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemSpecsUseCase;
import com.yd.vibecode.global.swagger.AdminProblemApi;
import com.yd.vibecode.global.common.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/problems")
public class AdminProblemController implements AdminProblemApi {

    private final GetProblemSpecsUseCase getProblemSpecsUseCase;

    @GetMapping("/{problemId}/specs")
    @Override
    public BaseResponse<List<ProblemSpecResponse>> getProblemSpecs(@PathVariable Long problemId) {
        List<ProblemSpecResponse> response = getProblemSpecsUseCase.execute(problemId);
        return BaseResponse.onSuccess(response);
    }
}

package com.yd.vibecode.domain.admin.ui;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.response.ProblemResponse;
import com.yd.vibecode.domain.admin.application.dto.response.ProblemSpecResponse;
import com.yd.vibecode.domain.admin.application.usecase.DeleteProblemUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemSpecsUseCase;
import com.yd.vibecode.domain.admin.application.usecase.GetProblemsUseCase;
import com.yd.vibecode.global.swagger.AdminProblemApi;
import com.yd.vibecode.global.common.BaseResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/problems")
public class AdminProblemController implements AdminProblemApi {

    private final GetProblemsUseCase getProblemsUseCase;
    private final DeleteProblemUseCase deleteProblemUseCase;
    private final GetProblemSpecsUseCase getProblemSpecsUseCase;

    @GetMapping
    @Override
    public BaseResponse<List<ProblemResponse>> getProblems() {
        List<ProblemResponse> response = getProblemsUseCase.execute();
        return BaseResponse.onSuccess(response);
    }

    @DeleteMapping("/{problemId}")
    @Override
    public BaseResponse<Void> deleteProblem(@PathVariable Long problemId) {
        deleteProblemUseCase.execute(problemId);
        return BaseResponse.onSuccess();
    }

    @GetMapping("/{problemId}/specs")
    @Override
    public BaseResponse<List<ProblemSpecResponse>> getProblemSpecs(@PathVariable Long problemId) {
        List<ProblemSpecResponse> response = getProblemSpecsUseCase.execute(problemId);
        return BaseResponse.onSuccess(response);
    }
}

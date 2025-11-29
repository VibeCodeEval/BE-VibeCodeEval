package com.yd.vibecode.domain.problem.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.problem.application.dto.response.AssignmentResponse;
import com.yd.vibecode.domain.problem.application.usecase.GetAssignmentUseCase;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.ProblemApi;

import lombok.RequiredArgsConstructor;

/**
 * Problem Controller
 * - GET /api/exams/{examId}/assignment: 배정 문제 조회 (USER)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exams")
public class ProblemController implements ProblemApi {

    private final GetAssignmentUseCase getAssignmentUseCase;

    @GetMapping("/{examId}/assignment")
    public BaseResponse<AssignmentResponse> getAssignment(
            @PathVariable Long examId,
            @CurrentUser Long userId) {
        AssignmentResponse response = getAssignmentUseCase.execute(examId, userId);
        return BaseResponse.onSuccess(response);
    }
}

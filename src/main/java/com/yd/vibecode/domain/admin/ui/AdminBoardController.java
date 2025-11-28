package com.yd.vibecode.domain.admin.ui;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.admin.application.dto.response.ExamineeBoardResponse;
import com.yd.vibecode.domain.admin.application.usecase.GetExamineeBoardUseCase;
import com.yd.vibecode.global.swagger.AdminBoardApi;
import com.yd.vibecode.global.common.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/board")
public class AdminBoardController implements AdminBoardApi {

    private final GetExamineeBoardUseCase getExamineeBoardUseCase;

    @GetMapping
    @Override
    public BaseResponse<List<ExamineeBoardResponse>> getBoard(@RequestParam Long examId) {
        List<ExamineeBoardResponse> response = getExamineeBoardUseCase.execute(examId);
        return BaseResponse.onSuccess(response);
    }
}

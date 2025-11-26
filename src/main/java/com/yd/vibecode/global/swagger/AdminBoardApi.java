package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.response.ExamineeBoardResponse;
import com.yd.vibecode.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "시험 보드", description = "시험 참가자 보드 조회 API")
public interface AdminBoardApi extends BaseApi {

    @Operation(summary = "시험 참가자 보드 조회", description = "시험에 참여 중인 응시자 현황을 조회합니다.")
    BaseResponse<List<ExamineeBoardResponse>> getBoard(Long examId);
}



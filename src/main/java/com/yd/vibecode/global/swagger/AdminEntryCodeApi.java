package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.admin.application.dto.request.CreateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.request.UpdateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "입장 코드", description = "입장 코드 발급 및 수정 API")
public interface AdminEntryCodeApi extends BaseApi {

    @Operation(summary = "입장 코드 생성", description = "관리자가 새로운 입장 코드를 발급합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    BaseResponse<EntryCodeResponse> createEntryCode(
            @Parameter(hidden = true) String adminId,
            CreateEntryCodeRequest request
    );

    @Operation(summary = "입장 코드 수정", description = "라벨, 만료일 등 입장 코드 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content())
    BaseResponse<EntryCodeResponse> updateEntryCode(String code, UpdateEntryCodeRequest request);

    @Operation(summary = "입장 코드 조회", description = "시험 ID로 입장 코드를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    BaseResponse<java.util.List<EntryCodeResponse>> getEntryCodes(Long examId, Boolean isActive);

    @Operation(summary = "입장 코드 삭제", description = "입장 코드를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    BaseResponse<Void> deleteEntryCode(String code);
}



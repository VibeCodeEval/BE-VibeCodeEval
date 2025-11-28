package com.yd.vibecode.global.swagger;

import com.yd.vibecode.domain.auth.application.dto.request.AdminLoginRequest;
import com.yd.vibecode.domain.auth.application.dto.request.AdminSignupRequest;
import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.application.dto.response.AdminLoginResponse;
import com.yd.vibecode.domain.auth.application.dto.response.EnterResponse;
import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.global.annotation.AccessToken;
import com.yd.vibecode.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증 관리", description = "사용자 인증, 회원가입, 로그인, 로그아웃")
public interface AuthApi extends BaseApi {

    @Operation(
            summary = "입장 (로그인/회원가입)",
            description = "입장 코드를 통해 시험에 입장합니다. 처음 입장 시 사용자가 생성됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "입장 성공",
                    content = @Content(schema = @Schema(implementation = EnterResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입장 코드 또는 만료된 코드",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<EnterResponse> enter(EnterRequest request);

    @Operation(
            summary = "관리자 회원가입",
            description = "새로운 관리자를 등록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (중복된 정보 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<Void> adminSignup(AdminSignupRequest request);

    @Operation(
            summary = "관리자 로그인",
            description = "관리자 계정으로 로그인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AdminLoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<AdminLoginResponse> adminLogin(AdminLoginRequest request);

    @Operation(
        summary = "관리자 로그아웃",
        description = "현재 AccessToken을 블랙리스트에 등록하고 RefreshToken을 제거합니다.",
        requestBody = @RequestBody(
            required = false,
            content = @Content(schema = @Schema(hidden = true))
        )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    BaseResponse<Void> adminLogout(@AccessToken String token);

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MeResponse.class))
            )
    })
    BaseResponse<MeResponse> me(@AccessToken String token);
}
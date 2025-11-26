package com.yd.vibecode.domain.auth.ui;

import com.yd.vibecode.domain.auth.application.dto.request.AdminLoginRequest;
import com.yd.vibecode.domain.auth.application.dto.request.AdminSignupRequest;
import com.yd.vibecode.domain.auth.application.dto.request.EnterRequest;
import com.yd.vibecode.domain.auth.application.dto.response.AdminLoginResponse;
import com.yd.vibecode.domain.auth.application.dto.response.EnterResponse;
import com.yd.vibecode.domain.auth.application.dto.response.MeResponse;
import com.yd.vibecode.domain.auth.application.usecase.AdminLoginUseCase;
import com.yd.vibecode.domain.auth.application.usecase.AdminLogoutUseCase;
import com.yd.vibecode.domain.auth.application.usecase.AdminSignupUseCase;
import com.yd.vibecode.domain.auth.application.usecase.EnterUseCase;
import com.yd.vibecode.domain.auth.application.usecase.MeUseCase;
import com.yd.vibecode.global.annotation.AccessToken;
import com.yd.vibecode.global.swagger.AuthApi;
import com.yd.vibecode.global.common.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final EnterUseCase enterUseCase;
    private final AdminSignupUseCase adminSignupUseCase;
    private final AdminLoginUseCase adminLoginUseCase;
    private final AdminLogoutUseCase adminLogoutUseCase;
    private final MeUseCase meUseCase;

    @PostMapping("/enter")
    public BaseResponse<EnterResponse> enter(@Valid @RequestBody EnterRequest request) {
        EnterResponse response = enterUseCase.execute(request);
        return BaseResponse.onSuccess(response);
    }

    @PostMapping("/admin/signup")
    public BaseResponse<Void> adminSignup(@Valid @RequestBody AdminSignupRequest request) {
        adminSignupUseCase.execute(request);
        return BaseResponse.onSuccess();
    }

    @PostMapping("/admin/login")
    public BaseResponse<AdminLoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminLoginUseCase.execute(request);
        return BaseResponse.onSuccess(response);
    }

    @PostMapping("/admin/logout")
    @Override
    public BaseResponse<Void> adminLogout(@AccessToken String token) {
        adminLogoutUseCase.execute(token);
        return BaseResponse.onSuccess();
    }

    @GetMapping("/me")
    public BaseResponse<MeResponse> me(@AccessToken String token) {
        MeResponse response = meUseCase.execute(token);
        return BaseResponse.onSuccess(response);
    }
}

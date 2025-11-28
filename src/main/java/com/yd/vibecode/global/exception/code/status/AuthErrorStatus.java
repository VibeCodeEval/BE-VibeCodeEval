package com.yd.vibecode.global.exception.code.status;

import org.springframework.http.HttpStatus;

import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseCodeInterface {

    EMPTY_JWT(HttpStatus.UNAUTHORIZED, "AUTH001", "JWT가 없습니다."),
    EXPIRED_MEMBER_JWT(HttpStatus.UNAUTHORIZED, "AUTH002", "만료된 JWT입니다."),
    UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "AUTH003", "지원하지 않는 JWT입니다."),

    INVALID_ID_TOKEN(HttpStatus.BAD_REQUEST, "AUTH004", "유효하지 않은 ID TOKEN입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH005", "만료된 REFRESH TOKEN입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "AUTH006", "유효하지 않은 ACCESS TOKEN입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH007", "유효하지 않은 REFRESH TOKEN입니다."),
    FAILED_SOCIAL_LOGIN(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH008", "소셜 로그인에 실패하였습니다."),
    LOGIN_ERROR(HttpStatus.BAD_REQUEST, "AUTH009", "잘못된 아이디 혹은 비밀번호입니다."),
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "AUTH010", "이미 가입된 이메일입니다."),
    ALREADY_REGISTERED_USER_ID(HttpStatus.BAD_REQUEST, "AUTH011", "이미 사용 중인 아이디입니다."),
    
    // Entry Code 관련
    INVALID_CODE(HttpStatus.BAD_REQUEST, "AUTH012", "입장코드가 유효하지 않습니다."),
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH013", "만료된 코드입니다."),
    CODE_CAP_REACHED(HttpStatus.BAD_REQUEST, "AUTH014", "정원 초과입니다."),
    
    // 권한 관련
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH015", "ADMIN 권한이 필요합니다."),
    
    // 토큰 한도 관련
    TOKEN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "AUTH016", "잔여 토큰이 부족합니다."),
    
    // 관리자 관련
    ALREADY_REGISTERED_ADMIN_NUMBER(HttpStatus.BAD_REQUEST, "AUTH017", "이미 사용 중인 관리자 번호입니다."),
    INVALID_ADMIN_NUMBER(HttpStatus.BAD_REQUEST, "AUTH018", "유효하지 않은 관리자 번호입니다."),
    ADMIN_NUMBER_INACTIVE(HttpStatus.BAD_REQUEST, "AUTH019", "사용할 수 없는 관리자 번호입니다."),
    MASTER_ONLY(HttpStatus.FORBIDDEN, "AUTH020", "MASTER 권한이 필요합니다.")
    ;

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCode getCode() {
        return BaseCode.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
}
package com.yd.vibecode.global.exception.code.status;

import org.springframework.http.HttpStatus;

import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProblemErrorStatus implements BaseCodeInterface {

    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PROBLEM001", "문제를 찾을 수 없습니다."),
    SPEC_NOT_FOUND(HttpStatus.NOT_FOUND, "PROBLEM002", "문제 스펙을 찾을 수 없습니다."),
    PROBLEM_NOT_PUBLISHED(HttpStatus.BAD_REQUEST, "PROBLEM003", "게시되지 않은 문제입니다."),
    SPEC_VERSION_CONFLICT(HttpStatus.CONFLICT, "PROBLEM004", "이미 해당 버전의 스펙이 존재합니다."),
    NO_ASSIGNED_PROBLEM(HttpStatus.BAD_REQUEST, "PROBLEM005", "배정된 문제가 없습니다.")
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

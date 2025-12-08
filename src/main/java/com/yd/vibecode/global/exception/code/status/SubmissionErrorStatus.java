package com.yd.vibecode.global.exception.code.status;

import org.springframework.http.HttpStatus;

import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubmissionErrorStatus implements BaseCodeInterface {

    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB001", "제출을 찾을 수 없습니다."),
    ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "SUB002", "이미 제출한 문제입니다."),
    INVALID_LANGUAGE(HttpStatus.BAD_REQUEST, "SUB003", "지원하지 않는 언어입니다."),
    CODE_TOO_LARGE(HttpStatus.BAD_REQUEST, "SUB004", "코드가 너무 큽니다."),
    QUEUE_FULL(HttpStatus.SERVICE_UNAVAILABLE, "SUB005", "채점 큐가 가득 찼습니다.")
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

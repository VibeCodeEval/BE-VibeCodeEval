package com.yd.vibecode.global.exception.code.status;

import org.springframework.http.HttpStatus;

import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExamErrorStatus implements BaseCodeInterface {

    EXAM_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAM001", "시험을 찾을 수 없습니다."),
    INVALID_EXAM_STATE(HttpStatus.BAD_REQUEST, "EXAM002", "잘못된 시험 상태입니다."),
    CANNOT_EXTEND_EXAM(HttpStatus.BAD_REQUEST, "EXAM003", "진행 중인 시험만 연장할 수 있습니다."),
    EXAM_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "EXAM004", "이미 시작된 시험입니다."),
    EXAM_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "EXAM005", "이미 종료된 시험입니다."),
    EXAM_NOT_STARTED(HttpStatus.BAD_REQUEST, "EXAM006", "시험이 아직 시작되지 않았습니다.")
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

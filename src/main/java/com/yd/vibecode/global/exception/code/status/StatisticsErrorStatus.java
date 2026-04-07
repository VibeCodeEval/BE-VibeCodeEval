package com.yd.vibecode.global.exception.code.status;

import org.springframework.http.HttpStatus;

import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatisticsErrorStatus implements BaseCodeInterface {

    STATISTICS_NOT_FOUND(HttpStatus.NOT_FOUND, "STAT001", "해당 시험의 통계 데이터가 없습니다.");

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

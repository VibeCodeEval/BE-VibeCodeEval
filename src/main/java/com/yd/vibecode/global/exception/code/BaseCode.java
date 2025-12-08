package com.yd.vibecode.global.exception.code;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class BaseCode {
    private final HttpStatus httpStatus;
    private final boolean isSuccess;
    private final String code;
    private final String message;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseCode baseCode = (BaseCode) o;
        return isSuccess == baseCode.isSuccess && httpStatus == baseCode.httpStatus && Objects.equals(code, baseCode.code) && Objects.equals(message, baseCode.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpStatus, isSuccess, code, message);
    }
}
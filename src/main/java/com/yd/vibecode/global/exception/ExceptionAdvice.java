package com.yd.vibecode.global.exception;

import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
@RequiredArgsConstructor
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handle500Exception(Exception e) {
        log.error("[handle500] unexpected exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /*
     * 직접 정의한 RestApiException 에러 클래스에 대한 예외 처리
     */
    // @ExceptionHandler는 Controller계층에서 발생하는 에러를 잡아서 메서드로 처리해주는 기능
    @ExceptionHandler(value = RestApiException.class)
    public ResponseEntity<BaseResponse<String>> handleRestApiException(RestApiException e) {
        log.warn("[handleRestApiException] code={} message={}",
                e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        BaseCode errorCode = e.getErrorCode();
        return handleExceptionInternal(errorCode);
    }

    /*
     * ConstraintViolationException 발생 시 예외 처리
     * 메서드 파라미터, 또는 메서드 리턴 값에 문제가 있을 경우, @Validated 검증 실패한 경우
     */
    @ExceptionHandler
    public ResponseEntity<BaseResponse<String>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("[handleConstraintViolation] validation failed: {}", e.getMessage());
        return handleExceptionInternal(GlobalErrorStatus._VALIDATION_ERROR.getCode());
    }

    /*
     * MethodArgumentTypeMismatchException 발생 시 예외 처리
     * 메서드의 인자 타입이 예상과 다른 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        // 예외 처리 로직
        log.warn("[handleTypeMismatch] param={} invalid type: {}", e.getName(), e.getValue());
        return handleExceptionInternal(GlobalErrorStatus._METHOD_ARGUMENT_ERROR.getCode());
    }

    /*
     *  MethodArgumentNotValidException 발생 시 예외 처리
     * @@RequestBody 내부에서 처리 실패한 경우, @Valid 검증 실패한 경우 (ArgumentResolver에 의해 유효성 검사)
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.warn("[handleNotValid] binding errors={}", e.getBindingResult().getFieldErrors());
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().stream()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage, (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        return handleExceptionInternalArgs(GlobalErrorStatus._VALIDATION_ERROR.getCode(), errors);

    }

    private ResponseEntity<BaseResponse<String>> handleExceptionInternal(BaseCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(BaseCode errorCode, Map<String, String> errorArgs) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorArgs));
    }

    private ResponseEntity<BaseResponse<String>> handleExceptionInternalFalse(BaseCode errorCode, String errorPoint) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorPoint));
    }
}
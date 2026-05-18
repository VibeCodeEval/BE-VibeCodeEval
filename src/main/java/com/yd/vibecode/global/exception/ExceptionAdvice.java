package com.yd.vibecode.global.exception;

import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import com.yd.vibecode.global.exception.code.status.SubmissionErrorStatus;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    /**
     * 동일 시험·참가자에 대한 제출은 DB 유니크(exam_id, participant_id)로 한 건만 허용된다.
     * 경쟁 조건 등으로 사전 검사를 통과한 뒤에도 위반이 나면 SUB002로 응답한다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        if (isDuplicateSubmissionConstraintViolation(e)) {
            log.warn("[handleDataIntegrityViolation] duplicate submission (exam_id, participant_id): {}",
                    e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage());
            return handleExceptionInternal(SubmissionErrorStatus.ALREADY_SUBMITTED.getCode());
        }
        String msg = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
        log.error("[handleDataIntegrityViolation] unmapped constraint: {}", msg, e);
        return handleExceptionInternal(GlobalErrorStatus._EXIST_ENTITY.getCode());
    }

    /**
     * submissions 테이블의 (exam_id, participant_id) 유니크 위반만 SUB002로 매핑한다.
     * submissions 테이블명·FK·NOT NULL 등 다른 무결성 오류는 제외한다.
     */
    static boolean isDuplicateSubmissionConstraintViolation(DataIntegrityViolationException e) {
        String text = extractConstraintViolationText(e).toLowerCase();
        if (text.isBlank()) {
            return false;
        }
        if (!text.contains("exam_id") || !text.contains("participant_id")) {
            return false;
        }
        return text.contains("duplicate")
                || text.contains("unique constraint")
                || text.contains("unique key")
                || text.contains("violates unique")
                || (text.contains("unique") && text.contains("constraint"))
                || text.contains("already exists");
    }

    private static String extractConstraintViolationText(DataIntegrityViolationException e) {
        StringBuilder sb = new StringBuilder();
        Throwable current = e;
        while (current != null) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage()).append(' ');
            }
            if (current instanceof org.hibernate.exception.ConstraintViolationException hibernateCve) {
                String constraintName = hibernateCve.getConstraintName();
                if (constraintName != null) {
                    sb.append(constraintName).append(' ');
                }
            }
            current = current.getCause();
        }
        return sb.toString().trim();
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
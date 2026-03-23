package com.tasklist.server.common.error;

import com.tasklist.server.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(resolveStatus(errorCode))
                .body(ApiResponse.failure(errorCode.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.unprocessableEntity()
                .body(ApiResponse.failure(ErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    @ExceptionHandler({BindException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleBindException(Exception exception) {
        return ResponseEntity.unprocessableEntity()
                .body(ApiResponse.failure(ErrorCode.VALIDATION_ERROR.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        String message = exception.getName() + " format is invalid";
        return ResponseEntity.unprocessableEntity()
                .body(ApiResponse.failure(ErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(ErrorCode.SYSTEM_ERROR.getCode(), exception.getMessage()));
    }

    private HttpStatus resolveStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

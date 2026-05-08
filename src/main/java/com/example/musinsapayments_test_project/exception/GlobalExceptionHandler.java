package com.example.musinsapayments_test_project.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler
 * (전역 예외 처리)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * PointException 처리
     *
     * @param e PointException
     * @return 에러 응답
     */
    @ExceptionHandler(PointException.class)
    public ResponseEntity<ErrorResponse> handlePointException(PointException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    /**
     * @Valid 검증 실패 처리
     *
     * @param e MethodArgumentNotValidException
     * @return 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, message));
    }

    /**
     * DB 무결성 제약 조건 위반 처리 (중복 키 등)
     *
     * @param e DataIntegrityViolationException
     * @return 에러 응답
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ResponseEntity
                .status(ErrorCode.DUPLICATE_POINT_KEY.getStatus())
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_POINT_KEY));
    }

    /**
     * 그 외 예외 처리
     *
     * @param e Exception
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * ErrorResponse
     * (에러 응답 DTO)
     */
    @Getter
    @Builder
    public static class ErrorResponse {

        private final String code;    // 에러 코드
        private final String message; // 에러 메시지

        public static ErrorResponse of(ErrorCode errorCode) {
            return ErrorResponse.builder()
                    .code(errorCode.name())
                    .message(errorCode.getMessage())
                    .build();
        }

        public static ErrorResponse of(ErrorCode errorCode, String message) {
            return ErrorResponse.builder()
                    .code(errorCode.name())
                    .message(message)
                    .build();
        }
    }
}
package com.example.musinsapayments_test_project.exception;

import lombok.Getter;

/**
 * PointException
 * (포인트 시스템 커스텀 예외)
 */
@Getter
public class PointException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * PointException 생성자
     *
     * @param errorCode 에러 코드
     */
    public PointException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
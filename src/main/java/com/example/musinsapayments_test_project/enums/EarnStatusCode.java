package com.example.musinsapayments_test_project.enums;

/**
 * EarnStatusCode
 * (포인트 적립 상태 코드)
 */
public enum EarnStatusCode {
    ACTIVE,    // 정상
    CANCELLED, // 취소
    EXPIRED;   // 만료

    /**
     * 적립 취소 가능 여부
     *
     * @return 취소 가능 여부
     */
    public boolean isCancellable() {
        return this == ACTIVE;
    }

    /**
     * 사용 가능 여부
     *
     * @return 사용 가능 여부
     */
    public boolean isUsable() {
        return this == ACTIVE;
    }
}
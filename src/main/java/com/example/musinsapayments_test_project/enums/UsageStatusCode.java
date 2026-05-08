package com.example.musinsapayments_test_project.enums;

/**
 * UsageStatusCode
 * (포인트 사용 상태 코드)
 */
public enum UsageStatusCode {
    USED,                // 사용
    PARTIALLY_CANCELLED, // 부분 취소
    CANCELLED;           // 취소

    /**
     * 사용 취소 가능 여부
     *
     * @return 취소 가능 여부
     */
    public boolean isCancellable() {
        return this == USED || this == PARTIALLY_CANCELLED;
    }
}
package com.example.musinsapayments_test_project.enums;

/**
 * EarnTypeCode
 * (포인트 적립 구분 코드)
 */
public enum EarnTypeCode {
    NORMAL, // 일반 적립
    MANUAL; // 수기 지급

    /**
     * 수기 지급 여부
     *
     * @return 수기 지급 여부
     */
    public boolean isManual() {
        return this == MANUAL;
    }
}
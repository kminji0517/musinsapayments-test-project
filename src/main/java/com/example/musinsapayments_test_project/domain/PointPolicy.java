package com.example.musinsapayments_test_project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PointPolicy 엔티티
 * (포인트 적립/보유 한도 정책 관리)
 */
@Entity
@Table(name = "point_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy {

    @Id
    @Column(name = "point_code")
    private String pointCode; // 정책 코드 (PK)

    @Column(name = "max_earn_amount", nullable = false)
    private Long maxEarnAmount; // 1회 최대 적립 가능 금액

    @Column(name = "max_hold_amount", nullable = false)
    private Long maxHoldAmount; // 최대 보유 가능 금액

    /**
     * PointPolicy 생성자
     *
     * @param pointCode     정책 코드
     * @param maxEarnAmount 1회 최대 적립 가능 금액
     * @param maxHoldAmount 최대 보유 가능 금액
     */
    @Builder
    public PointPolicy(String pointCode, Long maxEarnAmount, Long maxHoldAmount) {
        this.pointCode = pointCode;
        this.maxEarnAmount = maxEarnAmount;
        this.maxHoldAmount = maxHoldAmount;
    }
}
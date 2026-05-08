package com.example.musinsapayments_test_project.domain;

import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * PointEarn 엔티티
 * (포인트 적립 내역 관리)
 */
@Entity
@Table(name = "point_earn")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointEarn {

    @Id
    @Column(name = "point_key")
    private String pointKey; // 포인트 키 (PK)

    @Column(name = "member_id", nullable = false)
    private String memberId; // 회원 ID (FK)

    @Enumerated(EnumType.STRING)
    @Column(name = "earn_type_code", nullable = false)
    private EarnTypeCode earnTypeCode; // 적립 구분 코드

    @Enumerated(EnumType.STRING)
    @Column(name = "earn_status_code", nullable = false)
    private EarnStatusCode earnStatusCode; // 적립 상태 코드

    @Column(name = "earn_amount", nullable = false)
    private Long earnAmount; // 적립 금액

    @Column(name = "remaining_amount", nullable = false)
    private Long remainingAmount; // 잔여 금액

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 만료일자

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt; // 적립일자

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 취소일자

    @Column(name = "cancel_reason_code")
    private String cancelReasonCode; // 취소 사유 코드

    /**
     * PointEarn 생성자
     *
     * @param pointKey        포인트 키
     * @param memberId        회원 ID
     * @param earnTypeCode    적립 구분 코드
     * @param earnStatusCode  적립 상태 코드
     * @param earnAmount      적립 금액
     * @param remainingAmount 잔여 금액
     * @param expiredAt       만료일자
     * @param earnedAt        적립일자
     * @param cancelledAt     취소일자
     * @param cancelReasonCode 취소 사유 코드
     */
    @Builder
    public PointEarn(String pointKey, String memberId, EarnTypeCode earnTypeCode, EarnStatusCode earnStatusCode,
                     Long earnAmount, Long remainingAmount, LocalDateTime expiredAt, LocalDateTime earnedAt,
                     LocalDateTime cancelledAt, String cancelReasonCode) {
        this.pointKey = pointKey;
        this.memberId = memberId;
        this.earnTypeCode = earnTypeCode;
        this.earnStatusCode = earnStatusCode;
        this.earnAmount = earnAmount;
        this.remainingAmount = remainingAmount;
        this.expiredAt = expiredAt;
        this.earnedAt = earnedAt;
        this.cancelledAt = cancelledAt;
        this.cancelReasonCode = cancelReasonCode;
    }

    /**
     * 포인트 적립 취소
     *
     * @param cancelReasonCode 취소 사유 코드
     */
    public void cancel(String cancelReasonCode) {
        this.earnStatusCode = EarnStatusCode.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReasonCode = cancelReasonCode;
    }

    /**
     * 포인트 잔액 차감
     *
     * @param amount 차감 금액
     */
    public void deduct(Long amount) {
        this.remainingAmount -= amount;
    }

    /**
     * 포인트 잔액 복구
     *
     * @param amount 복구 금액
     */
    public void restore(Long amount) {
        this.remainingAmount += amount;
    }
}
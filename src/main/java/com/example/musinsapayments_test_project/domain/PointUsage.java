package com.example.musinsapayments_test_project.domain;

import com.example.musinsapayments_test_project.enums.UsageStatusCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * PointUsage 엔티티
 * (포인트 사용 내역 관리)
 */
@Entity
@Table(name = "point_usage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId; // 사용 ID (PK)

    @Column(name = "member_id", nullable = false)
    private String memberId; // 회원 ID (FK)

    @Column(name = "order_id", nullable = false)
    private String orderId; // 주문 ID (FK)

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_status_code", nullable = false)
    private UsageStatusCode usageStatusCode; // 사용 상태 코드

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount; // 총 사용 금액

    @Column(name = "remaining_cancel_amount", nullable = false)
    private Long remainingCancelAmount; // 취소 가능 잔여 금액

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt; // 사용일자

    @Column(name = "cancel_reason_code")
    private String cancelReasonCode; // 취소 사유 코드

    /**
     * PointUsage 생성자
     *
     * @param memberId              회원 ID
     * @param orderId               주문 ID
     * @param usageStatusCode       사용 상태 코드
     * @param totalAmount           총 사용 금액
     * @param remainingCancelAmount 취소 가능 잔여 금액
     * @param usedAt                사용일자
     */
    @Builder
    public PointUsage(String memberId, String orderId, UsageStatusCode usageStatusCode,
                      Long totalAmount, Long remainingCancelAmount, LocalDateTime usedAt) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.usageStatusCode = usageStatusCode;
        this.totalAmount = totalAmount;
        this.remainingCancelAmount = remainingCancelAmount;
        this.usedAt = usedAt;
    }

    /**
     * 포인트 사용 취소 금액 차감
     *
     * @param cancelAmount     취소 금액
     * @param cancelReasonCode 취소 사유 코드
     */
    public void cancel(Long cancelAmount, String cancelReasonCode) {
        this.remainingCancelAmount -= cancelAmount;
        this.cancelReasonCode = cancelReasonCode;
        if (this.remainingCancelAmount == 0) {
            this.usageStatusCode = UsageStatusCode.CANCELLED;
        } else {
            this.usageStatusCode = UsageStatusCode.PARTIALLY_CANCELLED;
        }
    }
}
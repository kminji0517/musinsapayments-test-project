package com.example.musinsapayments_test_project.dto;

import com.example.musinsapayments_test_project.domain.PointUsage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * UsePointResponse
 * (포인트 사용 응답 DTO)
 */
@Getter
@Builder
public class UsePointResponse {

    private Long usageId;              // 사용 ID
    private String memberId;           // 회원 ID
    private String orderId;            // 주문 ID
    private String usageStatusCode;    // 사용 상태 코드
    private Long totalAmount;          // 총 사용 금액
    private Long remainingCancelAmount; // 취소 가능 잔여 금액
    private LocalDateTime usedAt;      // 사용일자

    /**
     * PointUsage 엔티티로부터 UsePointResponse 생성
     *
     * @param pointUsage 포인트 사용 엔티티
     * @return UsePointResponse
     */
    public static UsePointResponse from(PointUsage pointUsage) {
        return UsePointResponse.builder()
                .usageId(pointUsage.getUsageId())
                .memberId(pointUsage.getMemberId())
                .orderId(pointUsage.getOrderId())
                .usageStatusCode(pointUsage.getUsageStatusCode())
                .totalAmount(pointUsage.getTotalAmount())
                .remainingCancelAmount(pointUsage.getRemainingCancelAmount())
                .usedAt(pointUsage.getUsedAt())
                .build();
    }
}
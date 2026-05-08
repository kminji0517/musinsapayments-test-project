package com.example.musinsapayments_test_project.dto;

import com.example.musinsapayments_test_project.domain.PointUsage;
import com.example.musinsapayments_test_project.enums.UsageStatusCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CancelUseResponse
 * (포인트 사용 취소 응답 DTO)
 */
@Getter
@Builder
public class CancelUseResponse {

    private Long usageId;                           // 사용 ID
    private String memberId;                        // 회원 ID
    private String orderId;                         // 주문 ID
    private UsageStatusCode usageStatusCode;        // 사용 상태 코드
    private Long totalAmount;                       // 총 사용 금액
    private Long remainingCancelAmount;             // 취소 가능 잔여 금액
    private Long cancelledAmount;                   // 취소 금액
    private List<String> newPointKeys;              // 만료로 인해 신규 적립된 포인트 키 목록
    private LocalDateTime usedAt;                   // 사용일자

    /**
     * PointUsage 엔티티로부터 CancelUseResponse 생성
     *
     * @param pointUsage    포인트 사용 엔티티
     * @param cancelledAmount 취소 금액
     * @param newPointKeys  신규 적립 포인트 키 목록
     * @return CancelUseResponse
     */
    public static CancelUseResponse of(PointUsage pointUsage, Long cancelledAmount, List<String> newPointKeys) {
        return CancelUseResponse.builder()
                .usageId(pointUsage.getUsageId())
                .memberId(pointUsage.getMemberId())
                .orderId(pointUsage.getOrderId())
                .usageStatusCode(pointUsage.getUsageStatusCode())
                .totalAmount(pointUsage.getTotalAmount())
                .remainingCancelAmount(pointUsage.getRemainingCancelAmount())
                .cancelledAmount(cancelledAmount)
                .newPointKeys(newPointKeys)
                .usedAt(pointUsage.getUsedAt())
                .build();
    }
}
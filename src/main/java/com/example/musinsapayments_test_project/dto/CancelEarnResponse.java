package com.example.musinsapayments_test_project.dto;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * CancelEarnResponse
 * (포인트 적립 취소 응답 DTO)
 */
@Getter
@Builder
public class CancelEarnResponse {

    private String pointKey;       // 포인트 키
    private String memberId;       // 회원 ID
    private EarnTypeCode earnTypeCode;   // 적립 구분 코드
    private EarnStatusCode earnStatusCode; // 적립 상태 코드
    private Long earnAmount;       // 적립 금액
    private Long remainingAmount;  // 잔여 금액
    private LocalDateTime earnedAt;    // 적립일자
    private LocalDateTime cancelledAt; // 취소일자

    /**
     * PointEarn 엔티티로부터 CancelEarnResponse 생성
     *
     * @param pointEarn 포인트 적립 엔티티
     * @return CancelEarnResponse
     */
    public static CancelEarnResponse from(PointEarn pointEarn) {
        return CancelEarnResponse.builder()
                .pointKey(pointEarn.getPointKey())
                .memberId(pointEarn.getMemberId())
                .earnTypeCode(pointEarn.getEarnTypeCode())
                .earnStatusCode(pointEarn.getEarnStatusCode())
                .earnAmount(pointEarn.getEarnAmount())
                .remainingAmount(pointEarn.getRemainingAmount())
                .earnedAt(pointEarn.getEarnedAt())
                .cancelledAt(pointEarn.getCancelledAt())
                .build();
    }
}
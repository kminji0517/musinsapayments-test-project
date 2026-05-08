package com.example.musinsapayments_test_project.dto;

import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * EarnPointRequest
 * (포인트 적립 요청 DTO)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarnPointRequest {

    @NotBlank
    private String memberId; // 회원 ID

    @NotBlank
    private String pointKey; // 포인트 키

    @NotNull
    private EarnTypeCode earnTypeCode; // 적립 구분 코드 (NORMAL, MANUAL)

    @NotNull
    private Long earnAmount; // 적립 금액

    private Integer expiredDays; // 만료일 (미입력 시 기본값 365일, 최소 1일, 최대 5년 미만)

    /**
     * 만료일 반환 (미입력 시 기본값 365일)
     *
     * @return 만료일
     */
    public int getExpiredDays() {
        return expiredDays != null ? expiredDays : 365;
    }
}
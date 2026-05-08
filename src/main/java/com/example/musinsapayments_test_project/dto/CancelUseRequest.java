package com.example.musinsapayments_test_project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CancelUseRequest
 * (포인트 사용 취소 요청 DTO)
 */
@Getter
@NoArgsConstructor
public class CancelUseRequest {

    @NotNull
    private Long usageId; // 사용 ID

    @NotNull
    @Min(1)
    private Long cancelAmount; // 취소 금액

    @NotBlank
    private String cancelReasonCode; // 취소 사유 코드
}
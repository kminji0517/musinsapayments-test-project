package com.example.musinsapayments_test_project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UsePointRequest
 * (포인트 사용 요청 DTO)
 */
@Getter
@NoArgsConstructor
public class UsePointRequest {

    @NotBlank
    private String memberId; // 회원 ID

    @NotBlank
    private String orderId; // 주문 ID

    @NotNull
    @Min(1)
    private Long useAmount; // 사용 금액
}
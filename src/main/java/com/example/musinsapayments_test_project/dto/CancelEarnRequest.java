package com.example.musinsapayments_test_project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CancelEarnRequest
 * (포인트 적립 취소 요청 DTO)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelEarnRequest {

    @NotBlank
    private String pointKey; // 포인트 키

    @NotBlank
    private String cancelReasonCode; // 취소 사유 코드
}
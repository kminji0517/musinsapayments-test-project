package com.example.musinsapayments_test_project.validator;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.domain.PointPolicy;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PointPolicyValidator
 * (포인트 정책 검증)
 */
@Component
@RequiredArgsConstructor
public class PointPolicyValidator {

    private final PointPolicyRepository pointPolicyRepository;
    private final PointEarnRepository pointEarnRepository;

    /**
     * 포인트 정책 검증
     * - 1회 최대 적립 금액 초과 여부
     * - 최대 보유 금액 초과 여부
     *
     * @param memberId   회원 ID
     * @param earnAmount 적립 금액
     */
    public void validate(String memberId, Long earnAmount) {
        PointPolicy policy = pointPolicyRepository.findById("DEFAULT")
                .orElseThrow(() -> new PointException(ErrorCode.POINT_POLICY_NOT_FOUND));

        if (earnAmount > policy.getMaxEarnAmount()) {
            throw new PointException(ErrorCode.EXCEED_MAX_EARN_AMOUNT);
        }

        List<PointEarn> activePoints = pointEarnRepository.findByMemberIdAndEarnStatusCode(memberId, "ACTIVE");
        long totalRemainingAmount = activePoints.stream()
                .mapToLong(PointEarn::getRemainingAmount)
                .sum();

        if (totalRemainingAmount + earnAmount > policy.getMaxHoldAmount()) {
            throw new PointException(ErrorCode.EXCEED_MAX_HOLD_AMOUNT);
        }
    }
}
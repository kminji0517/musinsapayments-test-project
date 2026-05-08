package com.example.musinsapayments_test_project.validator;

import com.example.musinsapayments_test_project.domain.PointUsage;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PointUsageCancelValidator
 * (포인트 사용 취소 검증)
 */
@Component
@RequiredArgsConstructor
public class PointUsageCancelValidator {

    private final PointUsageRepository pointUsageRepository;

    /**
     * 포인트 사용 취소 요청 검증
     * - 사용 내역 존재 여부
     * - 취소 가능 상태 여부
     * - 취소 가능 금액 초과 여부
     *
     * @param usageId      사용 ID
     * @param cancelAmount 취소 금액
     * @return PointUsage 포인트 사용 엔티티
     */
    public PointUsage validate(Long usageId, Long cancelAmount) {
        PointUsage pointUsage = pointUsageRepository.findById(usageId)
                .orElseThrow(() -> new PointException(ErrorCode.POINT_USAGE_NOT_FOUND));

        validateCancellable(pointUsage);
        validateCancelAmount(pointUsage, cancelAmount);

        return pointUsage;
    }

    private void validateCancellable(PointUsage pointUsage) {
        if (!pointUsage.getUsageStatusCode().isCancellable()) {
            throw new PointException(ErrorCode.POINT_USAGE_ALREADY_CANCELLED);
        }
    }

    private void validateCancelAmount(PointUsage pointUsage, Long cancelAmount) {
        if (cancelAmount > pointUsage.getRemainingCancelAmount()) {
            throw new PointException(ErrorCode.EXCEED_REMAINING_CANCEL_AMOUNT);
        }
    }
}
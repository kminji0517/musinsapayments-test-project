package com.example.musinsapayments_test_project.validator;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PointEarnCancelValidator
 * (포인트 적립 취소 검증)
 */
@Component
@RequiredArgsConstructor
public class PointEarnCancelValidator {

    private final PointEarnRepository pointEarnRepository;

    /**
     * 포인트 적립 취소 요청 검증
     * - 적립 내역 존재 여부, 이미 취소 여부, 만료 여부, 사용 여부
     *
     * @param pointKey 포인트 키
     */
    public void validate(String pointKey) {
        PointEarn pointEarn = pointEarnRepository.findByPointKey(pointKey)
                .orElseThrow(() -> new PointException(ErrorCode.POINT_EARN_NOT_FOUND));

        validateCancellable(pointEarn);
        validateNotExpired(pointEarn);
        validateNotUsed(pointEarn);
    }

    private void validateCancellable(PointEarn pointEarn) {
        if (!pointEarn.getEarnStatusCode().isCancellable()) {
            throw new PointException(ErrorCode.POINT_EARN_ALREADY_CANCELLED);
        }
    }

    private void validateNotExpired(PointEarn pointEarn) {
        if (pointEarn.getEarnStatusCode() == com.example.musinsapayments_test_project.enums.EarnStatusCode.EXPIRED) {
            throw new PointException(ErrorCode.POINT_EARN_ALREADY_EXPIRED);
        }
    }

    private void validateNotUsed(PointEarn pointEarn) {
        if (!pointEarn.getRemainingAmount().equals(pointEarn.getEarnAmount())) {
            throw new PointException(ErrorCode.POINT_EARN_ALREADY_USED);
        }
    }
}
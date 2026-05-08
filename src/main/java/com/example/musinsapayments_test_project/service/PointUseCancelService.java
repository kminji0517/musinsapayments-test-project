package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.domain.PointUsage;
import com.example.musinsapayments_test_project.domain.PointUsageDetail;
import com.example.musinsapayments_test_project.dto.CancelUseRequest;
import com.example.musinsapayments_test_project.dto.CancelUseResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.repository.PointUsageDetailRepository;
import com.example.musinsapayments_test_project.repository.PointUsageRepository;
import com.example.musinsapayments_test_project.validator.PointUsageCancelValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PointUseCancelService
 * (포인트 사용 취소 비즈니스 로직)
 */
@Service
@RequiredArgsConstructor
public class PointUseCancelService {

    private final PointUsageRepository pointUsageRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointEarnRepository pointEarnRepository;
    private final PointUsageCancelValidator pointUsageCancelValidator;

    /**
     * 포인트 사용 취소
     *
     * @param request 포인트 사용 취소 요청
     * @return 포인트 사용 취소 응답
     */
    @Transactional
    public CancelUseResponse cancel(CancelUseRequest request) {
        // 선검증 (사용 내역 존재 여부, 취소 가능 상태 여부, 취소 가능 금액 초과 여부)
        pointUsageCancelValidator.validate(request.getUsageId(), request.getCancelAmount());

        // 비관적 락으로 사용 내역 조회
        PointUsage pointUsage = pointUsageRepository.findByIdWithLock(request.getUsageId())
                .orElseThrow(() -> new PointException(ErrorCode.POINT_USAGE_NOT_FOUND));

        // 사용 상세 내역 조회
        List<PointUsageDetail> details = pointUsageDetailRepository.findByIdUsageId(request.getUsageId());

        // 취소 처리
        List<String> newPointKeys = new ArrayList<>();
        long remainingCancelAmount = request.getCancelAmount();

        for (PointUsageDetail detail : details) {
            if (remainingCancelAmount <= 0) break;

            // 해당 상세 내역에서 취소할 금액 계산
            long cancelAmount = Math.min(detail.getUsedAmount(), remainingCancelAmount);

            // 적립건 조회 (비관적 락)
            PointEarn pointEarn = pointEarnRepository.findByPointKeyWithLock(detail.getId().getPointKey())
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_EARN_NOT_FOUND));

            // 만료 여부 확인
            if (pointEarn.getExpiredAt().isBefore(LocalDateTime.now())) {
                // 만료된 포인트 → 신규 적립 처리
                String newPointKey = UUID.randomUUID().toString();
                PointEarn newPointEarn = PointEarn.builder()
                        .pointKey(newPointKey)
                        .memberId(pointUsage.getMemberId())
                        .earnTypeCode(pointEarn.getEarnTypeCode())
                        .earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(cancelAmount)
                        .remainingAmount(cancelAmount)
                        .expiredAt(LocalDateTime.now().plusDays(365)
                                .withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.now())
                        .build();
                pointEarnRepository.save(newPointEarn);
                newPointKeys.add(newPointKey);
            } else {
                // 유효한 포인트 → 잔액 복구
                pointEarn.restore(cancelAmount);
            }

            remainingCancelAmount -= cancelAmount;
        }

        // 사용 취소 금액 반영
        pointUsage.cancel(request.getCancelAmount(), request.getCancelReasonCode());

        return CancelUseResponse.of(pointUsage, request.getCancelAmount(), newPointKeys);
    }
}
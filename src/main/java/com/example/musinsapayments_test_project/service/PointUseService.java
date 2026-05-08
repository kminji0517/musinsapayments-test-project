package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.*;
import com.example.musinsapayments_test_project.dto.CancelUseRequest;
import com.example.musinsapayments_test_project.dto.CancelUseResponse;
import com.example.musinsapayments_test_project.dto.UsePointRequest;
import com.example.musinsapayments_test_project.dto.UsePointResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.UsageStatusCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.*;
import com.example.musinsapayments_test_project.validator.PointUsageCancelValidator;
import com.example.musinsapayments_test_project.validator.PointUseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PointUseService
 * (포인트 사용/사용취소 비즈니스 로직)
 */
@Service
@RequiredArgsConstructor
public class PointUseService {

    private final PointUsageRepository pointUsageRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointUsageCancelDetailRepository pointUsageCancelDetailRepository;
    private final PointEarnRepository pointEarnRepository;
    private final PointUseValidator pointUseValidator;
    private final PointUsageCancelValidator pointUsageCancelValidator;

    /**
     * 포인트 사용
     *
     * @param request 포인트 사용 요청
     * @return 포인트 사용 응답
     */
    @Transactional
    public UsePointResponse use(UsePointRequest request) {
        // 선검증 (회원, 주문 존재 여부, 중복 사용 여부, 잔액 부족)
        pointUseValidator.validate(request);

        // 비관적 락으로 사용 가능 포인트 조회 (동시 처리 방지)
        List<PointEarn> usablePoints = pointEarnRepository.findUsablePointsWithLock(
                request.getMemberId(), LocalDateTime.now());

        // 포인트 사용 내역 생성
        PointUsage pointUsage = PointUsage.builder()
                .memberId(request.getMemberId())
                .orderId(request.getOrderId())
                .usageStatusCode(UsageStatusCode.USED)
                .totalAmount(request.getUseAmount())
                .remainingCancelAmount(request.getUseAmount())
                .usedAt(LocalDateTime.now())
                .build();
        pointUsageRepository.save(pointUsage);

        // 적립건별 차감 및 사용 상세 내역 저장
        deductPoints(usablePoints, pointUsage, request.getUseAmount());

        return UsePointResponse.from(pointUsage);
    }

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

        // 사용 상세 내역 조회 (만료일 긴 순서로 복구)
        List<PointUsageDetail> details = pointUsageDetailRepository.findByIdUsageId(request.getUsageId());

        // 적립건별 취소 처리
        List<String> newPointKeys = processCancel(details, pointUsage, request);

        // 사용 취소 금액 반영
        pointUsage.cancel(request.getCancelAmount(), request.getCancelReasonCode());

        return CancelUseResponse.of(pointUsage, request.getCancelAmount(), newPointKeys);
    }

    /**
     * 적립건별 차감 및 사용 상세 내역 저장
     *
     * @param usablePoints 사용 가능 적립 내역 목록
     * @param pointUsage   포인트 사용 내역
     * @param useAmount    사용 금액
     */
    private void deductPoints(List<PointEarn> usablePoints, PointUsage pointUsage, Long useAmount) {
        List<PointUsageDetail> details = new ArrayList<>();
        long remainingUseAmount = useAmount;

        for (PointEarn pointEarn : usablePoints) {
            if (remainingUseAmount <= 0) break;

            long deductAmount = Math.min(pointEarn.getRemainingAmount(), remainingUseAmount);
            pointEarn.deduct(deductAmount);

            details.add(PointUsageDetail.builder()
                    .id(new PointUsageDetailId(pointUsage.getUsageId(), pointEarn.getPointKey()))
                    .usedAmount(deductAmount)
                    .usedAt(LocalDateTime.now())
                    .build());

            remainingUseAmount -= deductAmount;
        }

        pointUsageDetailRepository.saveAll(details);
    }

    /**
     * 적립건별 취소 처리
     *
     * @param details    사용 상세 내역 목록
     * @param pointUsage 포인트 사용 내역
     * @param request    포인트 사용 취소 요청
     * @return 신규 적립된 포인트 키 목록
     */
    private List<String> processCancel(List<PointUsageDetail> details, PointUsage pointUsage, CancelUseRequest request) {
        List<String> newPointKeys = new ArrayList<>();
        long remainingCancelAmount = request.getCancelAmount();

        for (PointUsageDetail detail : details) {
            if (remainingCancelAmount <= 0) break;

            // 취소 가능한 금액 계산
            Long alreadyCancelledAmount = pointUsageCancelDetailRepository
                    .sumCancelAmountByUsageIdAndPointKey(request.getUsageId(), detail.getId().getPointKey());
            long cancellableAmount = detail.getUsedAmount() - alreadyCancelledAmount;
            long cancelAmount = Math.min(cancellableAmount, remainingCancelAmount);

            if (cancelAmount <= 0) continue;

            // 적립건 조회 (비관적 락)
            PointEarn pointEarn = pointEarnRepository.findByPointKeyWithLock(detail.getId().getPointKey())
                    .orElseThrow(() -> new PointException(ErrorCode.POINT_EARN_NOT_FOUND));

            // 만료 여부에 따라 잔액 복구 또는 신규 적립
            String newPointKey = restoreOrReissue(pointEarn, pointUsage.getMemberId(), cancelAmount);
            if (newPointKey != null) newPointKeys.add(newPointKey);

            // 취소 상세 내역 저장
            saveCancelDetail(request.getUsageId(), detail.getId().getPointKey(), cancelAmount);

            remainingCancelAmount -= cancelAmount;
        }

        return newPointKeys;
    }

    /**
     * 만료 여부에 따라 잔액 복구 또는 신규 적립 처리
     *
     * @param pointEarn 포인트 적립 엔티티
     * @param memberId  회원 ID
     * @param amount    복구/신규 적립 금액
     * @return 신규 적립 시 포인트 키, 복구 시 null
     */
    private String restoreOrReissue(PointEarn pointEarn, String memberId, long amount) {
        if (pointEarn.getExpiredAt().isBefore(LocalDateTime.now())) {
            String newPointKey = UUID.randomUUID().toString();
            PointEarn newPointEarn = PointEarn.builder()
                    .pointKey(newPointKey)
                    .memberId(memberId)
                    .earnTypeCode(pointEarn.getEarnTypeCode())
                    .earnStatusCode(EarnStatusCode.ACTIVE)
                    .earnAmount(amount)
                    .remainingAmount(amount)
                    .expiredAt(LocalDateTime.now().plusDays(365)
                            .withHour(23).withMinute(59).withSecond(59))
                    .earnedAt(LocalDateTime.now())
                    .build();
            pointEarnRepository.save(newPointEarn);
            return newPointKey;
        }
        pointEarn.restore(amount);
        return null;
    }

    /**
     * 취소 상세 내역 저장
     *
     * @param usageId      사용 ID
     * @param pointKey     포인트 키
     * @param cancelAmount 취소 금액
     */
    private void saveCancelDetail(Long usageId, String pointKey, long cancelAmount) {
        long seq = pointUsageCancelDetailRepository
                .countByIdUsageIdAndIdPointKey(usageId, pointKey) + 1;

        pointUsageCancelDetailRepository.save(
                PointUsageCancelDetail.builder()
                        .id(new PointUsageCancelDetailId(usageId, pointKey, seq))
                        .cancelAmount(cancelAmount)
                        .cancelledAt(LocalDateTime.now())
                        .build()
        );
    }
}
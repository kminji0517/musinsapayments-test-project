package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.domain.PointUsage;
import com.example.musinsapayments_test_project.domain.PointUsageDetail;
import com.example.musinsapayments_test_project.domain.PointUsageDetailId;
import com.example.musinsapayments_test_project.dto.UsePointRequest;
import com.example.musinsapayments_test_project.dto.UsePointResponse;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.repository.PointUsageDetailRepository;
import com.example.musinsapayments_test_project.repository.PointUsageRepository;
import com.example.musinsapayments_test_project.validator.PointUseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PointUseService
 * (포인트 사용 비즈니스 로직)
 */
@Service
@RequiredArgsConstructor
public class PointUseService {

    private final PointUsageRepository pointUsageRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointEarnRepository pointEarnRepository;
    private final PointUseValidator pointUseValidator;

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
                .usageStatusCode("USED")
                .totalAmount(request.getUseAmount())
                .remainingCancelAmount(request.getUseAmount())
                .usedAt(LocalDateTime.now())
                .build();
        pointUsageRepository.save(pointUsage);

        // 적립건별 차감 및 사용 상세 내역 생성
        List<PointUsageDetail> details = new ArrayList<>();
        long remainingUseAmount = request.getUseAmount();

        for (PointEarn pointEarn : usablePoints) {
            if (remainingUseAmount <= 0) break;

            // 해당 적립건에서 차감할 금액 계산
            long deductAmount = Math.min(pointEarn.getRemainingAmount(), remainingUseAmount);

            // 적립건 잔액 차감 (더티 체킹으로 자동 UPDATE)
            pointEarn.deduct(deductAmount);

            // 사용 상세 내역 생성
            details.add(PointUsageDetail.builder()
                    .id(new PointUsageDetailId(pointUsage.getUsageId(), pointEarn.getPointKey()))
                    .usedAmount(deductAmount)
                    .usedAt(LocalDateTime.now())
                    .build());

            remainingUseAmount -= deductAmount;
        }

        pointUsageDetailRepository.saveAll(details);

        return UsePointResponse.from(pointUsage);
    }
}
package com.example.musinsapayments_test_project.loader;

import com.example.musinsapayments_test_project.domain.*;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.enums.UsageStatusCode;
import com.example.musinsapayments_test_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PointDataLoader
 * (포인트 정책/적립/사용/상세 샘플 데이터 적재)
 */
@Component
@RequiredArgsConstructor
public class PointDataLoader {

    private final PointPolicyRepository pointPolicyRepository;
    private final PointEarnRepository pointEarnRepository;
    private final PointUsageRepository pointUsageRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;

    /**
     * 포인트 정책 1건, 적립 내역 각 5~10건,
     * 사용 내역 각 2건, 사용 상세 6건 적재
     */
    public void load() {
        loadPointPolicy();
        loadPointEarn();
        loadPointUsage();
        loadPointUsageDetail();
    }

    private void loadPointPolicy() {
        pointPolicyRepository.save(
                PointPolicy.builder()
                        .pointCode("DEFAULT")
                        .maxEarnAmount(100000L)
                        .maxHoldAmount(1000000L)
                        .build()
        );
    }

    private void loadPointEarn() {
        pointEarnRepository.saveAll(List.of(
                // M001 적립 내역 (7건)
                PointEarn.builder().pointKey("PE001").memberId("M001").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.EXPIRED)
                        .earnAmount(1000L).remainingAmount(0L)
                        .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59))
                        .earnedAt(LocalDateTime.of(2024, 2, 1, 10, 0)).build(),
                PointEarn.builder().pointKey("PE002").memberId("M001").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(2000L).remainingAmount(300L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 3, 5, 11, 0)).build(),
                PointEarn.builder().pointKey("PE003").memberId("M001").earnTypeCode(EarnTypeCode.MANUAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(5000L).remainingAmount(5000L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 4, 10, 12, 0)).build(),
                PointEarn.builder().pointKey("PE004").memberId("M001").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(3000L).remainingAmount(3000L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 5, 15, 13, 0)).build(),
                PointEarn.builder().pointKey("PE005").memberId("M001").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.CANCELLED)
                        .earnAmount(1500L).remainingAmount(1500L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 6, 20, 14, 0))
                        .cancelledAt(LocalDateTime.of(2024, 7, 1, 10, 0)).cancelReasonCode("OWNER_CANCEL").build(),
                PointEarn.builder().pointKey("PE006").memberId("M001").earnTypeCode(EarnTypeCode.MANUAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(10000L).remainingAmount(10000L)
                        .expiredAt(LocalDateTime.now().plusYears(3).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 7, 1, 9, 0)).build(),
                PointEarn.builder().pointKey("PE007").memberId("M001").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(2500L).remainingAmount(2500L)
                        .expiredAt(LocalDateTime.now().plusYears(2).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 8, 1, 10, 0)).build(),

                // M002 적립 내역 (6건)
                PointEarn.builder().pointKey("PE008").memberId("M002").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.EXPIRED)
                        .earnAmount(3000L).remainingAmount(0L)
                        .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59))
                        .earnedAt(LocalDateTime.of(2024, 4, 1, 10, 0)).build(),
                PointEarn.builder().pointKey("PE009").memberId("M002").earnTypeCode(EarnTypeCode.MANUAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(8000L).remainingAmount(6000L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 5, 10, 11, 0)).build(),
                PointEarn.builder().pointKey("PE010").memberId("M002").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(4000L).remainingAmount(4000L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 6, 15, 12, 0)).build(),
                PointEarn.builder().pointKey("PE011").memberId("M002").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(2000L).remainingAmount(2000L)
                        .expiredAt(LocalDateTime.now().plusYears(2).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 7, 20, 13, 0)).build(),
                PointEarn.builder().pointKey("PE012").memberId("M002").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(6000L).remainingAmount(6000L)
                        .expiredAt(LocalDateTime.now().plusYears(2).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 8, 25, 14, 0)).build(),
                PointEarn.builder().pointKey("PE013").memberId("M002").earnTypeCode(EarnTypeCode.MANUAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(15000L).remainingAmount(15000L)
                        .expiredAt(LocalDateTime.now().plusYears(3).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 9, 1, 9, 0)).build(),

                // M003 적립 내역 (5건)
                PointEarn.builder().pointKey("PE014").memberId("M003").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(5000L).remainingAmount(5000L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 7, 1, 10, 0)).build(),
                PointEarn.builder().pointKey("PE015").memberId("M003").earnTypeCode(EarnTypeCode.MANUAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(20000L).remainingAmount(15000L)
                        .expiredAt(LocalDateTime.now().plusYears(3).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 8, 5, 11, 0)).build(),
                PointEarn.builder().pointKey("PE016").memberId("M003").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(3000L).remainingAmount(3000L)
                        .expiredAt(LocalDateTime.now().plusYears(1).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 9, 10, 12, 0)).build(),
                PointEarn.builder().pointKey("PE017").memberId("M003").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.EXPIRED)
                        .earnAmount(2000L).remainingAmount(0L)
                        .expiredAt(LocalDateTime.of(2024, 12, 31, 23, 59))
                        .earnedAt(LocalDateTime.of(2024, 10, 15, 13, 0)).build(),
                PointEarn.builder().pointKey("PE018").memberId("M003").earnTypeCode(EarnTypeCode.NORMAL).earnStatusCode(EarnStatusCode.ACTIVE)
                        .earnAmount(4000L).remainingAmount(4000L)
                        .expiredAt(LocalDateTime.now().plusYears(2).withHour(23).withMinute(59).withSecond(59))
                        .earnedAt(LocalDateTime.of(2024, 11, 20, 14, 0)).build()
        ));
    }

    private void loadPointUsage() {
        pointUsageRepository.saveAll(List.of(
                // M001 사용 내역 (2건)
                PointUsage.builder().memberId("M001").orderId("O001").usageStatusCode(UsageStatusCode.USED)
                        .totalAmount(1000L).remainingCancelAmount(1000L)
                        .usedAt(LocalDateTime.of(2024, 2, 1, 11, 0)).build(),
                PointUsage.builder().memberId("M001").orderId("O002").usageStatusCode(UsageStatusCode.CANCELLED)
                        .totalAmount(1700L).remainingCancelAmount(0L)
                        .usedAt(LocalDateTime.of(2024, 3, 5, 12, 0)).build(),

                // M002 사용 내역 (2건)
                PointUsage.builder().memberId("M002").orderId("O006").usageStatusCode(UsageStatusCode.USED)
                        .totalAmount(3000L).remainingCancelAmount(0L)
                        .usedAt(LocalDateTime.of(2024, 4, 1, 11, 0)).build(),
                PointUsage.builder().memberId("M002").orderId("O007").usageStatusCode(UsageStatusCode.USED)
                        .totalAmount(2000L).remainingCancelAmount(2000L)
                        .usedAt(LocalDateTime.of(2024, 5, 10, 12, 0)).build(),

                // M003 사용 내역 (2건)
                PointUsage.builder().memberId("M003").orderId("O011").usageStatusCode(UsageStatusCode.USED)
                        .totalAmount(2000L).remainingCancelAmount(2000L)
                        .usedAt(LocalDateTime.of(2024, 7, 1, 11, 0)).build(),
                PointUsage.builder().memberId("M003").orderId("O012").usageStatusCode(UsageStatusCode.CANCELLED)
                        .totalAmount(3000L).remainingCancelAmount(0L)
                        .usedAt(LocalDateTime.of(2024, 8, 5, 12, 0)).build()
        ));
    }

    private void loadPointUsageDetail() {
        pointUsageDetailRepository.saveAll(List.of(
                PointUsageDetail.builder().id(new PointUsageDetailId(1L, "PE001")).usedAmount(1000L).usedAt(LocalDateTime.of(2024, 2, 1, 11, 0)).build(),
                PointUsageDetail.builder().id(new PointUsageDetailId(2L, "PE002")).usedAmount(1700L).usedAt(LocalDateTime.of(2024, 3, 5, 12, 0)).build(),
                PointUsageDetail.builder().id(new PointUsageDetailId(3L, "PE008")).usedAmount(3000L).usedAt(LocalDateTime.of(2024, 4, 1, 11, 0)).build(),
                PointUsageDetail.builder().id(new PointUsageDetailId(4L, "PE009")).usedAmount(2000L).usedAt(LocalDateTime.of(2024, 5, 10, 12, 0)).build(),
                PointUsageDetail.builder().id(new PointUsageDetailId(5L, "PE015")).usedAmount(2000L).usedAt(LocalDateTime.of(2024, 7, 1, 11, 0)).build(),
                PointUsageDetail.builder().id(new PointUsageDetailId(6L, "PE015")).usedAmount(3000L).usedAt(LocalDateTime.of(2024, 8, 5, 12, 0)).build()
        ));
    }
}
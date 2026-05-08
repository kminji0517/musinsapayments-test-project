package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointUsageCancelDetail;
import com.example.musinsapayments_test_project.domain.PointUsageCancelDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * PointUsageCancelDetailRepository
 * (포인트 사용 취소 상세 내역 데이터 접근)
 */
public interface PointUsageCancelDetailRepository extends JpaRepository<PointUsageCancelDetail, PointUsageCancelDetailId> {

    /**
     * 사용 ID와 포인트 키로 취소 상세 내역 개수 조회 (seq 채번용)
     *
     * @param usageId  사용 ID
     * @param pointKey 포인트 키
     * @return 취소 상세 내역 개수
     */
    long countByIdUsageIdAndIdPointKey(Long usageId, String pointKey);

    /**
     * 사용 ID와 포인트 키로 취소 금액 합계 조회
     *
     * @param usageId  사용 ID
     * @param pointKey 포인트 키
     * @return 취소 금액 합계
     */
    @Query("SELECT COALESCE(SUM(pcd.cancelAmount), 0) FROM PointUsageCancelDetail pcd " +
            "WHERE pcd.id.usageId = :usageId AND pcd.id.pointKey = :pointKey")
    Long sumCancelAmountByUsageIdAndPointKey(@Param("usageId") Long usageId, @Param("pointKey") String pointKey);
}
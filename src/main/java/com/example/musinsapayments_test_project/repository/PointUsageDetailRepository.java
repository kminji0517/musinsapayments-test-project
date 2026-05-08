package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointUsageDetail;
import com.example.musinsapayments_test_project.domain.PointUsageDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * PointUsageDetailRepository
 * (포인트 사용 상세 내역 데이터 접근)
 */
public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, PointUsageDetailId> {

    /**
     * 사용 ID로 포인트 사용 상세 내역 조회 (만료일 긴 순)
     *
     * @param usageId 사용 ID
     * @return 포인트 사용 상세 내역 목록
     */
    @Query("SELECT pud FROM PointUsageDetail pud " +
            "JOIN PointEarn pe ON pud.id.pointKey = pe.pointKey " +
            "WHERE pud.id.usageId = :usageId " +
            "ORDER BY pe.expiredAt DESC")
    List<PointUsageDetail> findByIdUsageId(@Param("usageId") Long usageId);
}
package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointUsageDetail;
import com.example.musinsapayments_test_project.domain.PointUsageDetailId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PointUsageDetailRepository
 * (포인트 사용 상세 내역 데이터 접근)
 */
public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, PointUsageDetailId> {

    /**
     * 사용 ID로 포인트 사용 상세 내역 조회
     *
     * @param usageId 사용 ID
     * @return 포인트 사용 상세 내역 목록
     */
    List<PointUsageDetail> findByIdUsageId(Long usageId);
}
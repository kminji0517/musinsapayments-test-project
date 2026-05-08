package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointUsageDetail;
import com.example.musinsapayments_test_project.domain.PointUsageDetailId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PointUsageDetailRepository
 * (포인트 사용 상세 내역 데이터 접근)
 */
public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, PointUsageDetailId> {
}
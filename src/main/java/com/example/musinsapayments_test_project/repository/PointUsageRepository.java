package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointUsage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PointUsageRepository
 * (포인트 사용 내역 데이터 접근)
 */
public interface PointUsageRepository extends JpaRepository<PointUsage, Long> {

    boolean existsByOrderId(String orderId);
}
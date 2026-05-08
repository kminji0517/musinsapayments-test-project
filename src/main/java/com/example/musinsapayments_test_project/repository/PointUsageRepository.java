package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointUsage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * PointUsageRepository
 * (포인트 사용 내역 데이터 접근)
 */
public interface PointUsageRepository extends JpaRepository<PointUsage, Long> {

    boolean existsByOrderId(String orderId);
    /**
     * 사용 ID로 포인트 사용 내역 조회 (비관적 락)
     *
     * @param usageId 사용 ID
     * @return 포인트 사용 내역
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pu FROM PointUsage pu WHERE pu.usageId = :usageId")
    Optional<PointUsage> findByIdWithLock(@Param("usageId") Long usageId);
}
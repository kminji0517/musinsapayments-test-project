package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PointPolicyRepository
 * (포인트 정책 데이터 접근)
 */
public interface PointPolicyRepository extends JpaRepository<PointPolicy, String> {
}
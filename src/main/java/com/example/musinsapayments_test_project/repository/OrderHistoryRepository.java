package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * OrderHistoryRepository
 * (주문 내역 데이터 접근)
 */
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, String> {
}
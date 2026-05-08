package com.example.musinsapayments_test_project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * OrderHistory 엔티티
 * (주문 내역 관리)
 */
@Entity
@Table(name = "order_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderHistory {

    @Id
    @Column(name = "order_id")
    private String orderId; // 주문 ID (PK)

    @Column(name = "member_id", nullable = false)
    private String memberId; // 회원 ID (FK)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 주문일자

    /**
     * OrderHistory 생성자
     *
     * @param orderId   주문 ID
     * @param memberId  회원 ID
     * @param createdAt 주문일자
     */
    @Builder
    public OrderHistory(String orderId, String memberId, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.memberId = memberId;
        this.createdAt = createdAt;
    }
}
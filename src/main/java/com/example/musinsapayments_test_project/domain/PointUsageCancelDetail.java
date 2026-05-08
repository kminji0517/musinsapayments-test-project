package com.example.musinsapayments_test_project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * PointUsageCancelDetail 엔티티
 * (포인트 사용 취소 상세 내역 관리)
 */
@Entity
@Table(name = "point_usage_cancel_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageCancelDetail {

    @EmbeddedId
    private PointUsageCancelDetailId id; // 복합 PK (usage_id + point_key + seq)

    @Column(name = "cancel_amount", nullable = false)
    private Long cancelAmount; // 취소 금액

    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt; // 취소일자

    /**
     * PointUsageCancelDetail 생성자
     *
     * @param id           복합 PK
     * @param cancelAmount 취소 금액
     * @param cancelledAt  취소일자
     */
    @Builder
    public PointUsageCancelDetail(PointUsageCancelDetailId id, Long cancelAmount, LocalDateTime cancelledAt) {
        this.id = id;
        this.cancelAmount = cancelAmount;
        this.cancelledAt = cancelledAt;
    }
}
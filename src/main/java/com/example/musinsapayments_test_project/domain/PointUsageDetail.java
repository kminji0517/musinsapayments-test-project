package com.example.musinsapayments_test_project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * PointUsageDetail 엔티티
 * (포인트 사용 상세 내역 관리)
 */
@Entity
@Table(name = "point_usage_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageDetail {

    @EmbeddedId
    private PointUsageDetailId id; // 복합 PK (usage_id + point_key)

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount; // 사용 금액

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt; // 사용일자

    /**
     * PointUsageDetail 생성자
     *
     * @param id         복합 PK
     * @param usedAmount 사용 금액
     * @param usedAt     사용일자
     */
    @Builder
    public PointUsageDetail(PointUsageDetailId id, Long usedAmount, LocalDateTime usedAt) {
        this.id = id;
        this.usedAmount = usedAmount;
        this.usedAt = usedAt;
    }
}
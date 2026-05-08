package com.example.musinsapayments_test_project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * PointUsageDetail 복합 PK
 * (usage_id + point_key)
 */
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class PointUsageDetailId implements Serializable {

    @Column(name = "usage_id")
    private Long usageId; // 사용 ID (FK)

    @Column(name = "point_key")
    private String pointKey; // 포인트 키 (FK)

    /**
     * PointUsageDetailId 생성자
     *
     * @param usageId  사용 ID
     * @param pointKey 포인트 키
     */
    public PointUsageDetailId(Long usageId, String pointKey) {
        this.usageId = usageId;
        this.pointKey = pointKey;
    }
}
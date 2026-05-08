package com.example.musinsapayments_test_project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * PointUsageCancelDetail 복합 PK
 * (usage_id + point_key + seq)
 */
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class PointUsageCancelDetailId implements Serializable {

    @Column(name = "usage_id")
    private Long usageId; // 사용 ID (FK)

    @Column(name = "point_key")
    private String pointKey; // 포인트 키 (FK)

    @Column(name = "seq")
    private Long seq; // 일련번호

    public PointUsageCancelDetailId(Long usageId, String pointKey, Long seq) {
        this.usageId = usageId;
        this.pointKey = pointKey;
        this.seq = seq;
    }
}
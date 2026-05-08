package com.example.musinsapayments_test_project.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.time.LocalDateTime;

/**
 * Member 엔티티
 * (회원 기본정보 관리)
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @Column(name = "member_id")
    private String memberId; // 회원 ID (PK)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 가입일자

    /**
     * Member 생성자
     *
     * @param memberId  회원 ID
     * @param createdAt 가입일자
     */
    @Builder
    public Member(String memberId, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.createdAt = createdAt;
    }
}
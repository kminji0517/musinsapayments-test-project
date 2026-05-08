package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointEarn;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * PointEarnRepository
 * (포인트 적립 내역 데이터 접근)
 */
public interface PointEarnRepository extends JpaRepository<PointEarn, String> {

    /**
     * 회원 ID와 적립 상태 코드로 포인트 적립 내역 조회 (비관적 락)
     * 최대 보유 금액 검증 시 동시 처리 방지를 위해 락 적용
     *
     * @param memberId       회원 ID
     * @param earnStatusCode 적립 상태 코드
     * @return 포인트 적립 내역 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pe FROM PointEarn pe WHERE pe.memberId = :memberId AND pe.earnStatusCode = :earnStatusCode")
    List<PointEarn> findByMemberIdAndEarnStatusCodeWithLock(
            @Param("memberId") String memberId,
            @Param("earnStatusCode") String earnStatusCode
    );

    /**
     * 회원 ID와 적립 상태 코드로 포인트 적립 내역 조회 (락 없음)
     *
     * @param memberId       회원 ID
     * @param earnStatusCode 적립 상태 코드
     * @return 포인트 적립 내역 목록
     */
    List<PointEarn> findByMemberIdAndEarnStatusCode(String memberId, String earnStatusCode);
}
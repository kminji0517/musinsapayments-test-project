package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.PointEarn;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PointEarnRepository
 * (포인트 적립 내역 데이터 접근)
 */
public interface PointEarnRepository extends JpaRepository<PointEarn, String> {

    /**
     * 회원 ID와 적립 상태 코드로 포인트 적립 내역 조회
     *
     * @param memberId       회원 ID
     * @param earnStatusCode 적립 상태 코드
     * @return 포인트 적립 내역 목록
     */
    List<PointEarn> findByMemberIdAndEarnStatusCode(String memberId, String earnStatusCode);

    /**
     * 포인트 키로 포인트 적립 내역 조회
     *
     * @param pointKey 포인트 키
     * @return 포인트 적립 내역
     */
    @Query("SELECT pe FROM PointEarn pe WHERE pe.pointKey = :pointKey")
    Optional<PointEarn> findByPointKey(@Param("pointKey") String pointKey);

    /**
     * 포인트 키로 포인트 적립 내역 조회 (비관적 락)
     *
     * @param pointKey 포인트 키
     * @return 포인트 적립 내역
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pe FROM PointEarn pe WHERE pe.pointKey = :pointKey")
    Optional<PointEarn> findByPointKeyWithLock(@Param("pointKey") String pointKey);

    /**
     * 사용 가능한 적립 내역 조회
     * - ACTIVE 상태, 만료되지 않은 것
     * - 수기 지급(MANUAL) 우선, 만료일 짧은 순 정렬
     *
     * @param memberId 회원 ID
     * @param now      현재 시각
     * @return 사용 가능한 적립 내역 목록
     */
    @Query("SELECT pe FROM PointEarn pe " +
            "WHERE pe.memberId = :memberId " +
            "AND pe.earnStatusCode = 'ACTIVE' " +
            "AND pe.expiredAt > :now " +
            "AND pe.remainingAmount > 0 " +
            "ORDER BY " +
            "CASE WHEN pe.earnTypeCode = 'MANUAL' THEN 0 ELSE 1 END ASC, " +
            "pe.expiredAt ASC")
    List<PointEarn> findUsablePoints(@Param("memberId") String memberId, @Param("now") LocalDateTime now);

    /**
     * 사용 가능한 적립 내역 조회 (비관적 락)
     * - ACTIVE 상태, 만료되지 않은 것
     * - 수기 지급(MANUAL) 우선, 만료일 짧은 순 정렬
     *
     * @param memberId 회원 ID
     * @param now      현재 시각
     * @return 사용 가능한 적립 내역 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pe FROM PointEarn pe " +
            "WHERE pe.memberId = :memberId " +
            "AND pe.earnStatusCode = 'ACTIVE' " +
            "AND pe.expiredAt > :now " +
            "AND pe.remainingAmount > 0 " +
            "ORDER BY " +
            "CASE WHEN pe.earnTypeCode = 'MANUAL' THEN 0 ELSE 1 END ASC, " +
            "pe.expiredAt ASC")
    List<PointEarn> findUsablePointsWithLock(@Param("memberId") String memberId, @Param("now") LocalDateTime now);
}
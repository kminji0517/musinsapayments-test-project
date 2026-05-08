package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.dto.EarnPointRequest;
import com.example.musinsapayments_test_project.dto.EarnPointResponse;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.validator.PointEarnValidator;
import com.example.musinsapayments_test_project.validator.PointPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * PointEarnService
 * (포인트 적립 비즈니스 로직)
 */
@Service
@RequiredArgsConstructor
public class PointEarnService {

    private final PointEarnRepository pointEarnRepository;
    private final PointEarnValidator pointEarnValidator;
    private final PointPolicyValidator pointPolicyValidator;

    /**
     * 포인트 적립
     *
     * @param request 포인트 적립 요청
     * @return 포인트 적립 응답
     */
    @Transactional
    public EarnPointResponse earn(EarnPointRequest request) {
        // 선검증 (회원 존재 여부, 적립 구분 코드, 만료일 범위, 포인트 키 중복)
        pointEarnValidator.validate(request);

        // 포인트 정책 검증
        pointPolicyValidator.validate(request.getMemberId(), request.getEarnAmount());

        // 만료일 계산
        LocalDateTime expiredAt = LocalDateTime.now()
                .plusDays(request.getExpiredDays())
                .withHour(23).withMinute(59).withSecond(59);

        // 포인트 적립
        PointEarn pointEarn = PointEarn.builder()
                .pointKey(request.getPointKey())
                .memberId(request.getMemberId())
                .earnTypeCode(request.getEarnTypeCode())
                .earnStatusCode("ACTIVE")
                .earnAmount(request.getEarnAmount())
                .remainingAmount(request.getEarnAmount())
                .expiredAt(expiredAt)
                .earnedAt(LocalDateTime.now())
                .build();

        pointEarnRepository.save(pointEarn);

        return EarnPointResponse.from(pointEarn);
    }
}
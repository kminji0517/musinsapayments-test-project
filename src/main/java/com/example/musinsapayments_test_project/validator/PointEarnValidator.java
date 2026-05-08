package com.example.musinsapayments_test_project.validator;

import com.example.musinsapayments_test_project.dto.EarnPointRequest;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.MemberRepository;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PointEarnValidator
 * (포인트 적립 검증)
 */
@Component
@RequiredArgsConstructor
public class PointEarnValidator {

    private static final int MIN_EXPIRED_DAYS = 1;
    private static final int MAX_EXPIRED_DAYS = 365 * 5 - 1; // 5년 미만

    private final MemberRepository memberRepository;
    private final PointEarnRepository pointEarnRepository;

    /**
     * 포인트 적립 요청 검증
     * - 회원 존재 여부, 만료일 범위, 포인트 키 중복
     *
     * @param request 포인트 적립 요청
     */
    public void validate(EarnPointRequest request) {
        validateMember(request.getMemberId());
        validateExpiredDaysRange(request.getExpiredDays());
        validateDuplicatePointKey(request.getPointKey());
    }

    private void validateMember(String memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new PointException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateExpiredDaysRange(Integer expiredDays) {
        if (expiredDays < MIN_EXPIRED_DAYS || expiredDays >= MAX_EXPIRED_DAYS) {
            throw new PointException(ErrorCode.INVALID_EXPIRED_DAYS);
        }
    }

    private void validateDuplicatePointKey(String pointKey) {
        if (pointEarnRepository.existsById(pointKey)) {
            throw new PointException(ErrorCode.DUPLICATE_POINT_KEY);
        }
    }
}
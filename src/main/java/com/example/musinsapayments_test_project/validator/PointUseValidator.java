package com.example.musinsapayments_test_project.validator;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.dto.UsePointRequest;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.MemberRepository;
import com.example.musinsapayments_test_project.repository.OrderHistoryRepository;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.repository.PointUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PointUseValidator
 * (포인트 사용 검증)
 */
@Component
@RequiredArgsConstructor
public class PointUseValidator {

    private final MemberRepository memberRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final PointEarnRepository pointEarnRepository;
    private final PointUsageRepository pointUsageRepository;

    /**
     * 포인트 사용 요청 검증
     * - 회원 존재 여부
     * - 주문 존재 여부
     * - 주문 중복 사용 여부 (멱등성)
     * - 포인트 잔액 부족 여부
     *
     * @param request 포인트 사용 요청
     * @return 사용 가능한 적립 내역 목록 (수기 우선, 만료일 짧은 순)
     */
    public List<PointEarn> validate(UsePointRequest request) {
        validateMember(request.getMemberId());
        validateOrder(request.getOrderId());
        validateDuplicateOrder(request.getOrderId());

        List<PointEarn> usablePoints = getUsablePoints(request.getMemberId());
        validateSufficientPoint(usablePoints, request.getUseAmount());

        return usablePoints;
    }

    private void validateMember(String memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new PointException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateOrder(String orderId) {
        orderHistoryRepository.findById(orderId)
                .orElseThrow(() -> new PointException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateDuplicateOrder(String orderId) {
        if (pointUsageRepository.existsByOrderId(orderId)) {
            throw new PointException(ErrorCode.DUPLICATE_ORDER_USAGE);
        }
    }

    private void validateSufficientPoint(List<PointEarn> usablePoints, Long useAmount) {
        long totalRemaining = usablePoints.stream()
                .mapToLong(PointEarn::getRemainingAmount)
                .sum();
        if (totalRemaining < useAmount) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT);
        }
    }

    /**
     * 사용 가능한 적립 내역 조회
     * - ACTIVE 상태
     * - 만료일이 지나지 않은 것
     * - 수기 지급(MANUAL) 우선, 만료일 짧은 순 정렬
     *
     * @param memberId 회원 ID
     * @return 사용 가능한 적립 내역 목록
     */
    private List<PointEarn> getUsablePoints(String memberId) {
        return pointEarnRepository.findUsablePoints(memberId, LocalDateTime.now());
    }
}
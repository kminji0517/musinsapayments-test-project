package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.domain.PointUsage;
import com.example.musinsapayments_test_project.dto.UsePointRequest;
import com.example.musinsapayments_test_project.dto.UsePointResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.enums.UsageStatusCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.repository.PointUsageDetailRepository;
import com.example.musinsapayments_test_project.repository.PointUsageRepository;
import com.example.musinsapayments_test_project.validator.PointUseValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointUseServiceTest {

    @InjectMocks
    private PointUseService pointUseService;

    @Mock
    private PointUsageRepository pointUsageRepository;

    @Mock
    private PointUsageDetailRepository pointUsageDetailRepository;

    @Mock
    private PointEarnRepository pointEarnRepository;

    @Mock
    private PointUseValidator pointUseValidator;

    private PointEarn createPointEarn(String pointKey, EarnTypeCode earnTypeCode,
                                      Long remainingAmount, LocalDateTime expiredAt) {
        return PointEarn.builder()
                .pointKey(pointKey)
                .memberId("M001")
                .earnTypeCode(earnTypeCode)
                .earnStatusCode(EarnStatusCode.ACTIVE)
                .earnAmount(remainingAmount)
                .remainingAmount(remainingAmount)
                .expiredAt(expiredAt)
                .earnedAt(LocalDateTime.now())
                .build();
    }

    private PointUsage createPointUsage(Long usageId, Long totalAmount) {
        return PointUsage.builder()
                .memberId("M001")
                .orderId("O001")
                .usageStatusCode(UsageStatusCode.USED)
                .totalAmount(totalAmount)
                .remainingCancelAmount(totalAmount)
                .usedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Case1 : 포인트 사용 - 정상")
    void use_success() {
        // given
        List<PointEarn> usablePoints = List.of(
                createPointEarn("PE001", EarnTypeCode.NORMAL, 5000L, LocalDateTime.now().plusDays(365))
        );

        when(pointEarnRepository.findUsablePointsWithLock(anyString(), any()))
                .thenReturn(usablePoints);
        when(pointUsageRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M001")
                .orderId("O001")
                .useAmount(3000L)
                .build();

        // when
        UsePointResponse response = pointUseService.use(request);

        // then
        assertThat(response.getMemberId()).isEqualTo("M001");
        assertThat(response.getOrderId()).isEqualTo("O001");
        assertThat(response.getTotalAmount()).isEqualTo(3000L);
        assertThat(response.getUsageStatusCode()).isEqualTo(UsageStatusCode.USED);
    }

    @Test
    @DisplayName("Case2 : 포인트 사용 (존재하지 않는 회원) - 오류")
    void use_member_not_found() {
        // given
        doThrow(new PointException(ErrorCode.MEMBER_NOT_FOUND))
                .when(pointUseValidator).validate(any());

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M999")
                .orderId("O001")
                .useAmount(3000L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointUseService.use(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Case3 : 포인트 사용 (존재하지 않는 주문) - 오류")
    void use_order_not_found() {
        // given
        doThrow(new PointException(ErrorCode.ORDER_NOT_FOUND))
                .when(pointUseValidator).validate(any());

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M001")
                .orderId("O999")
                .useAmount(3000L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointUseService.use(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Case4 : 포인트 사용 (중복 주문) - 오류")
    void use_duplicate_order() {
        // given
        doThrow(new PointException(ErrorCode.DUPLICATE_ORDER_USAGE))
                .when(pointUseValidator).validate(any());

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M001")
                .orderId("O001")
                .useAmount(3000L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointUseService.use(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.DUPLICATE_ORDER_USAGE.getMessage());
    }

    @Test
    @DisplayName("Case5 : 포인트 사용 (잔액 부족) - 오류")
    void use_insufficient_point() {
        // given
        doThrow(new PointException(ErrorCode.INSUFFICIENT_POINT))
                .when(pointUseValidator).validate(any());

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M001")
                .orderId("O001")
                .useAmount(9999999L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointUseService.use(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_POINT.getMessage());
    }

    @Test
    @DisplayName("Case6 : 포인트 사용 (수기 지급 포인트 우선 사용) - 정상")
    void use_manual_point_first() {
        // given
        PointEarn manualPoint = createPointEarn("PE001", EarnTypeCode.MANUAL, 5000L,
                LocalDateTime.now().plusDays(365));
        PointEarn normalPoint = createPointEarn("PE002", EarnTypeCode.NORMAL, 5000L,
                LocalDateTime.now().plusDays(180));

        when(pointEarnRepository.findUsablePointsWithLock(anyString(), any()))
                .thenReturn(List.of(manualPoint, normalPoint));
        when(pointUsageRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M001")
                .orderId("O001")
                .useAmount(3000L)
                .build();

        // when
        pointUseService.use(request);

        // then
        assertThat(manualPoint.getRemainingAmount()).isEqualTo(2000L); // 5000 - 3000
        assertThat(normalPoint.getRemainingAmount()).isEqualTo(5000L); // 차감 안됨
    }

    @Test
    @DisplayName("Case7 : 포인트 사용 (만료일 짧은 순서로 사용) - 정상")
    void use_order_by_expired_at() {
        // given
        PointEarn soonExpiredPoint = createPointEarn("PE001", EarnTypeCode.NORMAL, 5000L,
                LocalDateTime.now().plusDays(30));
        PointEarn lateExpiredPoint = createPointEarn("PE002", EarnTypeCode.NORMAL, 5000L,
                LocalDateTime.now().plusDays(365));

        when(pointEarnRepository.findUsablePointsWithLock(anyString(), any()))
                .thenReturn(List.of(soonExpiredPoint, lateExpiredPoint));
        when(pointUsageRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M001")
                .orderId("O001")
                .useAmount(3000L)
                .build();

        // when
        pointUseService.use(request);

        // then
        assertThat(soonExpiredPoint.getRemainingAmount()).isEqualTo(2000L); // 5000 - 3000
        assertThat(lateExpiredPoint.getRemainingAmount()).isEqualTo(5000L); // 차감 안됨
    }

    @Test
    @DisplayName("Case8 : 포인트 사용 (여러 적립건에서 차감) - 정상")
    void use_multiple_point_earn() {
        // given
        PointEarn point1 = createPointEarn("PE001", EarnTypeCode.NORMAL, 2000L,
                LocalDateTime.now().plusDays(30));
        PointEarn point2 = createPointEarn("PE002", EarnTypeCode.NORMAL, 5000L,
                LocalDateTime.now().plusDays(365));

        when(pointEarnRepository.findUsablePointsWithLock(anyString(), any()))
                .thenReturn(List.of(point1, point2));
        when(pointUsageRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsePointRequest request = UsePointRequest.builder()
                .memberId("M001")
                .orderId("O001")
                .useAmount(5000L)
                .build();

        // when
        pointUseService.use(request);

        // then
        assertThat(point1.getRemainingAmount()).isEqualTo(0L); // 2000 전액 차감
        assertThat(point2.getRemainingAmount()).isEqualTo(2000L); // 5000 - 3000
    }
}
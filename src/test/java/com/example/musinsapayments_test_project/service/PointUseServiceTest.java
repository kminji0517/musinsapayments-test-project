package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.*;
import com.example.musinsapayments_test_project.dto.CancelUseRequest;
import com.example.musinsapayments_test_project.dto.CancelUseResponse;
import com.example.musinsapayments_test_project.dto.UsePointRequest;
import com.example.musinsapayments_test_project.dto.UsePointResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.enums.UsageStatusCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.*;
import com.example.musinsapayments_test_project.validator.PointUsageCancelValidator;
import com.example.musinsapayments_test_project.validator.PointUseValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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
    private PointUsageCancelDetailRepository pointUsageCancelDetailRepository;

    @Mock
    private PointEarnRepository pointEarnRepository;

    @Mock
    private PointUseValidator pointUseValidator;

    @Mock
    private PointUsageCancelValidator pointUsageCancelValidator;

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

    private PointUsage createPointUsage(Long totalAmount) {
        return PointUsage.builder()
                .memberId("M001")
                .orderId("O001")
                .usageStatusCode(UsageStatusCode.USED)
                .totalAmount(totalAmount)
                .remainingCancelAmount(totalAmount)
                .usedAt(LocalDateTime.now())
                .build();
    }

    // ==================== 포인트 사용 ====================

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
        assertThat(manualPoint.getRemainingAmount()).isEqualTo(2000L);
        assertThat(normalPoint.getRemainingAmount()).isEqualTo(5000L);
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
        assertThat(soonExpiredPoint.getRemainingAmount()).isEqualTo(2000L);
        assertThat(lateExpiredPoint.getRemainingAmount()).isEqualTo(5000L);
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
        assertThat(point1.getRemainingAmount()).isEqualTo(0L);
        assertThat(point2.getRemainingAmount()).isEqualTo(2000L);
    }

    // ==================== 포인트 사용 취소 ====================

    @Test
    @DisplayName("Case9 : 포인트 사용 취소 - 정상 (전체 취소)")
    void cancel_success_full() {
        // given
        PointUsage pointUsage = createPointUsage(3000L);
        PointEarn pointEarn = createPointEarn("PE001", EarnTypeCode.NORMAL, 2000L,
                LocalDateTime.now().plusDays(365));

        PointUsageDetail detail = PointUsageDetail.builder()
                .id(new PointUsageDetailId(1L, "PE001"))
                .usedAmount(3000L)
                .usedAt(LocalDateTime.now())
                .build();

        when(pointUsageCancelValidator.validate(anyLong(), anyLong())).thenReturn(pointUsage);
        when(pointUsageRepository.findByIdWithLock(anyLong())).thenReturn(Optional.of(pointUsage));
        when(pointUsageDetailRepository.findByIdUsageId(anyLong())).thenReturn(List.of(detail));
        when(pointEarnRepository.findByPointKeyWithLock(anyString())).thenReturn(Optional.of(pointEarn));
        when(pointUsageCancelDetailRepository.countByIdUsageIdAndIdPointKey(anyLong(), anyString())).thenReturn(0L);
        when(pointUsageCancelDetailRepository.sumCancelAmountByUsageIdAndPointKey(anyLong(), anyString())).thenReturn(0L);

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(3000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelUseResponse response = pointUseService.cancel(request);

        // then
        assertThat(response.getCancelledAmount()).isEqualTo(3000L);
        assertThat(response.getUsageStatusCode()).isEqualTo(UsageStatusCode.CANCELLED);
        assertThat(pointEarn.getRemainingAmount()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("Case10 : 포인트 사용 취소 - 정상 (부분 취소)")
    void cancel_success_partial() {
        // given
        PointUsage pointUsage = createPointUsage(3000L);
        PointEarn pointEarn = createPointEarn("PE001", EarnTypeCode.NORMAL, 2000L,
                LocalDateTime.now().plusDays(365));

        PointUsageDetail detail = PointUsageDetail.builder()
                .id(new PointUsageDetailId(1L, "PE001"))
                .usedAmount(3000L)
                .usedAt(LocalDateTime.now())
                .build();

        when(pointUsageCancelValidator.validate(anyLong(), anyLong())).thenReturn(pointUsage);
        when(pointUsageRepository.findByIdWithLock(anyLong())).thenReturn(Optional.of(pointUsage));
        when(pointUsageDetailRepository.findByIdUsageId(anyLong())).thenReturn(List.of(detail));
        when(pointEarnRepository.findByPointKeyWithLock(anyString())).thenReturn(Optional.of(pointEarn));
        when(pointUsageCancelDetailRepository.countByIdUsageIdAndIdPointKey(anyLong(), anyString())).thenReturn(0L);
        when(pointUsageCancelDetailRepository.sumCancelAmountByUsageIdAndPointKey(anyLong(), anyString())).thenReturn(0L);

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(1000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelUseResponse response = pointUseService.cancel(request);

        // then
        assertThat(response.getCancelledAmount()).isEqualTo(1000L);
        assertThat(response.getUsageStatusCode()).isEqualTo(UsageStatusCode.PARTIALLY_CANCELLED);
        assertThat(pointEarn.getRemainingAmount()).isEqualTo(3000L);
    }

    @Test
    @DisplayName("Case11 : 포인트 사용 취소 - 정상 (만료된 포인트 신규 적립)")
    void cancel_success_expired_point_new_earn() {
        // given
        PointUsage pointUsage = createPointUsage(3000L);
        PointEarn expiredPointEarn = createPointEarn("PE001", EarnTypeCode.NORMAL, 0L,
                LocalDateTime.now().minusDays(1));

        PointUsageDetail detail = PointUsageDetail.builder()
                .id(new PointUsageDetailId(1L, "PE001"))
                .usedAmount(3000L)
                .usedAt(LocalDateTime.now())
                .build();

        when(pointUsageCancelValidator.validate(anyLong(), anyLong())).thenReturn(pointUsage);
        when(pointUsageRepository.findByIdWithLock(anyLong())).thenReturn(Optional.of(pointUsage));
        when(pointUsageDetailRepository.findByIdUsageId(anyLong())).thenReturn(List.of(detail));
        when(pointEarnRepository.findByPointKeyWithLock(anyString())).thenReturn(Optional.of(expiredPointEarn));
        when(pointEarnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pointUsageCancelDetailRepository.countByIdUsageIdAndIdPointKey(anyLong(), anyString())).thenReturn(0L);
        when(pointUsageCancelDetailRepository.sumCancelAmountByUsageIdAndPointKey(anyLong(), anyString())).thenReturn(0L);

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(3000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelUseResponse response = pointUseService.cancel(request);

        // then
        assertThat(response.getCancelledAmount()).isEqualTo(3000L);
        assertThat(response.getNewPointKeys()).hasSize(1);
    }

    @Test
    @DisplayName("Case12 : 포인트 사용 취소 (부분 취소 후 나머지 추가 취소) - 정상")
    void cancel_success_partial_then_remaining() {
        // given
        PointUsage pointUsage = PointUsage.builder()
                .memberId("M001")
                .orderId("O001")
                .usageStatusCode(UsageStatusCode.PARTIALLY_CANCELLED)
                .totalAmount(3000L)
                .remainingCancelAmount(2000L) // 이미 1000원 취소된 상태
                .usedAt(LocalDateTime.now())
                .build();

        PointEarn pointEarn = createPointEarn("PE001", EarnTypeCode.NORMAL, 2000L,
                LocalDateTime.now().plusDays(365));

        PointUsageDetail detail = PointUsageDetail.builder()
                .id(new PointUsageDetailId(1L, "PE001"))
                .usedAmount(3000L)
                .usedAt(LocalDateTime.now())
                .build();

        when(pointUsageCancelValidator.validate(anyLong(), anyLong())).thenReturn(pointUsage);
        when(pointUsageRepository.findByIdWithLock(anyLong())).thenReturn(Optional.of(pointUsage));
        when(pointUsageDetailRepository.findByIdUsageId(anyLong())).thenReturn(List.of(detail));
        when(pointEarnRepository.findByPointKeyWithLock(anyString())).thenReturn(Optional.of(pointEarn));
        when(pointUsageCancelDetailRepository.countByIdUsageIdAndIdPointKey(anyLong(), anyString())).thenReturn(1L);
        when(pointUsageCancelDetailRepository.sumCancelAmountByUsageIdAndPointKey(anyLong(), anyString())).thenReturn(1000L);

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(2000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelUseResponse response = pointUseService.cancel(request);

        // then
        assertThat(response.getCancelledAmount()).isEqualTo(2000L);
        assertThat(response.getUsageStatusCode()).isEqualTo(UsageStatusCode.CANCELLED);
        assertThat(pointEarn.getRemainingAmount()).isEqualTo(4000L);
    }

    @Test
    @DisplayName("Case13 : 포인트 사용 취소 (존재하지 않는 사용 내역) - 오류")
    void cancel_usage_not_found() {
        // given
        when(pointUsageCancelValidator.validate(anyLong(), anyLong()))
                .thenThrow(new PointException(ErrorCode.POINT_USAGE_NOT_FOUND));

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(999L)
                .cancelAmount(1000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when & then
        assertThatThrownBy(() -> pointUseService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_USAGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Case14 : 포인트 사용 취소 (이미 취소된 사용 내역) - 오류")
    void cancel_already_cancelled() {
        // given
        when(pointUsageCancelValidator.validate(anyLong(), anyLong()))
                .thenThrow(new PointException(ErrorCode.POINT_USAGE_ALREADY_CANCELLED));

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(1000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when & then
        assertThatThrownBy(() -> pointUseService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_USAGE_ALREADY_CANCELLED.getMessage());
    }

    @Test
    @DisplayName("Case15 : 포인트 사용 취소 (취소 가능 금액 초과) - 오류")
    void cancel_exceed_remaining_cancel_amount() {
        // given
        when(pointUsageCancelValidator.validate(anyLong(), anyLong()))
                .thenThrow(new PointException(ErrorCode.EXCEED_REMAINING_CANCEL_AMOUNT));

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(9999999L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when & then
        assertThatThrownBy(() -> pointUseService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.EXCEED_REMAINING_CANCEL_AMOUNT.getMessage());
    }
}
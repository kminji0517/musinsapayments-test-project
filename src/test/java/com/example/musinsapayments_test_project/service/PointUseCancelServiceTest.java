package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.domain.PointUsage;
import com.example.musinsapayments_test_project.domain.PointUsageDetail;
import com.example.musinsapayments_test_project.domain.PointUsageDetailId;
import com.example.musinsapayments_test_project.dto.CancelUseRequest;
import com.example.musinsapayments_test_project.dto.CancelUseResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.enums.UsageStatusCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.repository.PointUsageDetailRepository;
import com.example.musinsapayments_test_project.repository.PointUsageRepository;
import com.example.musinsapayments_test_project.validator.PointUsageCancelValidator;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointUseCancelServiceTest {

    @InjectMocks
    private PointUseCancelService pointUseCancelService;

    @Mock
    private PointUsageRepository pointUsageRepository;

    @Mock
    private PointUsageDetailRepository pointUsageDetailRepository;

    @Mock
    private PointEarnRepository pointEarnRepository;

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

    @Test
    @DisplayName("Case1 : 포인트 사용 취소 - 정상 (전체 취소)")
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

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(3000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelUseResponse response = pointUseCancelService.cancel(request);

        // then
        assertThat(response.getCancelledAmount()).isEqualTo(3000L);
        assertThat(response.getUsageStatusCode()).isEqualTo(UsageStatusCode.CANCELLED);
        assertThat(pointEarn.getRemainingAmount()).isEqualTo(5000L); // 2000 + 3000 복구
    }

    @Test
    @DisplayName("Case2 : 포인트 사용 취소 - 정상 (부분 취소)")
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

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(1000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelUseResponse response = pointUseCancelService.cancel(request);

        // then
        assertThat(response.getCancelledAmount()).isEqualTo(1000L);
        assertThat(response.getUsageStatusCode()).isEqualTo(UsageStatusCode.PARTIALLY_CANCELLED);
        assertThat(pointEarn.getRemainingAmount()).isEqualTo(3000L); // 2000 + 1000 복구
    }

    @Test
    @DisplayName("Case3 : 포인트 사용 취소 - 정상 (만료된 포인트 신규 적립)")
    void cancel_success_expired_point_new_earn() {
        // given
        PointUsage pointUsage = createPointUsage(3000L);
        PointEarn expiredPointEarn = createPointEarn("PE001", EarnTypeCode.NORMAL, 0L,
                LocalDateTime.now().minusDays(1)); // 만료된 포인트

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

        CancelUseRequest request = CancelUseRequest.builder()
                .usageId(1L)
                .cancelAmount(3000L)
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelUseResponse response = pointUseCancelService.cancel(request);

        // then
        assertThat(response.getCancelledAmount()).isEqualTo(3000L);
        assertThat(response.getNewPointKeys()).hasSize(1); // 신규 적립 1건
    }

    @Test
    @DisplayName("Case4 : 포인트 사용 취소 (존재하지 않는 사용 내역) - 오류")
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
        assertThatThrownBy(() -> pointUseCancelService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_USAGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Case5 : 포인트 사용 취소 (이미 취소된 사용 내역) - 오류")
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
        assertThatThrownBy(() -> pointUseCancelService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_USAGE_ALREADY_CANCELLED.getMessage());
    }

    @Test
    @DisplayName("Case6 : 포인트 사용 취소 (취소 가능 금액 초과) - 오류")
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
        assertThatThrownBy(() -> pointUseCancelService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.EXCEED_REMAINING_CANCEL_AMOUNT.getMessage());
    }
}
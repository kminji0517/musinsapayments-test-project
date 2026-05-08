package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.dto.CancelEarnRequest;
import com.example.musinsapayments_test_project.dto.CancelEarnResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.validator.PointEarnCancelValidator;
import com.example.musinsapayments_test_project.validator.PointEarnValidator;
import com.example.musinsapayments_test_project.validator.PointPolicyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointEarnCancelServiceTest {

    @InjectMocks
    private PointEarnService pointEarnService;

    @Mock
    private PointEarnRepository pointEarnRepository;

    @Mock
    private PointEarnCancelValidator pointEarnCancelValidator;

    @Mock
    private PointPolicyValidator pointPolicyValidator;

    @Mock
    private PointEarnValidator pointEarnValidator;

    private PointEarn createPointEarn(String pointKey, EarnTypeCode earnTypeCode,
                                      EarnStatusCode earnStatusCode, Long earnAmount,
                                      Long remainingAmount, LocalDateTime expiredAt) {
        return PointEarn.builder()
                .pointKey(pointKey)
                .memberId("M001")
                .earnTypeCode(earnTypeCode)
                .earnStatusCode(earnStatusCode)
                .earnAmount(earnAmount)
                .remainingAmount(remainingAmount)
                .expiredAt(expiredAt)
                .earnedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Case1 : 포인트 적립 취소 - 정상")
    void cancel_success() {
        // given
        PointEarn pointEarn = createPointEarn("PE001", EarnTypeCode.NORMAL, EarnStatusCode.ACTIVE,
                5000L, 5000L, LocalDateTime.now().plusDays(365));

        when(pointEarnRepository.findByPointKeyWithLock(anyString()))
                .thenReturn(Optional.of(pointEarn));

        CancelEarnRequest request = CancelEarnRequest.builder()
                .pointKey("PE001")
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when
        CancelEarnResponse response = pointEarnService.cancel(request);

        // then
        assertThat(response.getPointKey()).isEqualTo("PE001");
        assertThat(response.getEarnStatusCode()).isEqualTo(EarnStatusCode.CANCELLED);
        assertThat(response.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("Case2 : 포인트 적립 취소 (존재하지 않는 포인트 키) - 오류")
    void cancel_point_earn_not_found() {
        // given
        doThrow(new PointException(ErrorCode.POINT_EARN_NOT_FOUND))
                .when(pointEarnCancelValidator).validate(anyString());

        CancelEarnRequest request = CancelEarnRequest.builder()
                .pointKey("PE999")
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_EARN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Case3 : 포인트 적립 취소 (이미 취소된 포인트) - 오류")
    void cancel_already_cancelled() {
        // given
        doThrow(new PointException(ErrorCode.POINT_EARN_ALREADY_CANCELLED))
                .when(pointEarnCancelValidator).validate(anyString());

        CancelEarnRequest request = CancelEarnRequest.builder()
                .pointKey("PE001")
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_EARN_ALREADY_CANCELLED.getMessage());
    }

    @Test
    @DisplayName("Case4 : 포인트 적립 취소 (만료된 포인트) - 오류")
    void cancel_already_expired() {
        // given
        doThrow(new PointException(ErrorCode.POINT_EARN_ALREADY_EXPIRED))
                .when(pointEarnCancelValidator).validate(anyString());

        CancelEarnRequest request = CancelEarnRequest.builder()
                .pointKey("PE001")
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_EARN_ALREADY_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("Case5 : 포인트 적립 취소 (사용된 포인트) - 오류")
    void cancel_already_used() {
        // given
        doThrow(new PointException(ErrorCode.POINT_EARN_ALREADY_USED))
                .when(pointEarnCancelValidator).validate(anyString());

        CancelEarnRequest request = CancelEarnRequest.builder()
                .pointKey("PE001")
                .cancelReasonCode("OWNER_CANCEL")
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.cancel(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.POINT_EARN_ALREADY_USED.getMessage());
    }
}
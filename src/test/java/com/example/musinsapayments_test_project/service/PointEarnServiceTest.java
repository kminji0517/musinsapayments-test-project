package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.domain.PointEarn;
import com.example.musinsapayments_test_project.dto.CancelEarnRequest;
import com.example.musinsapayments_test_project.dto.CancelEarnResponse;
import com.example.musinsapayments_test_project.dto.EarnPointRequest;
import com.example.musinsapayments_test_project.dto.EarnPointResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.repository.PointPolicyRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointEarnServiceTest {

    @InjectMocks
    private PointEarnService pointEarnService;

    @Mock
    private PointEarnRepository pointEarnRepository;

    @Mock
    private PointPolicyRepository pointPolicyRepository;

    @Mock
    private PointEarnValidator pointEarnValidator;

    @Mock
    private PointEarnCancelValidator pointEarnCancelValidator;

    @Mock
    private PointPolicyValidator pointPolicyValidator;

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

    // ==================== 포인트 적립 ====================

    @Test
    @DisplayName("Case1 : 포인트 적립 - 정상")
    void earn_success() {
        // given
        when(pointEarnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EarnPointRequest request = EarnPointRequest.builder()
                .memberId("M001")
                .pointKey("PE999")
                .earnTypeCode(EarnTypeCode.NORMAL)
                .earnAmount(5000L)
                .expiredDays(365)
                .build();

        // when
        EarnPointResponse response = pointEarnService.earn(request);

        // then
        assertThat(response.getPointKey()).isEqualTo("PE999");
        assertThat(response.getMemberId()).isEqualTo("M001");
        assertThat(response.getEarnTypeCode()).isEqualTo(EarnTypeCode.NORMAL);
        assertThat(response.getEarnStatusCode()).isEqualTo(EarnStatusCode.ACTIVE);
        assertThat(response.getEarnAmount()).isEqualTo(5000L);
        assertThat(response.getRemainingAmount()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("Case2 : 포인트 적립 (존재하지 않는 회원) - 오류")
    void earn_member_not_found() {
        // given
        doThrow(new PointException(ErrorCode.MEMBER_NOT_FOUND))
                .when(pointEarnValidator).validate(any());

        EarnPointRequest request = EarnPointRequest.builder()
                .memberId("M999")
                .pointKey("PE999")
                .earnTypeCode(EarnTypeCode.NORMAL)
                .earnAmount(5000L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.earn(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Case3 : 포인트 적립 (중복 포인트 키) - 오류")
    void earn_duplicate_point_key() {
        // given
        doThrow(new PointException(ErrorCode.DUPLICATE_POINT_KEY))
                .when(pointEarnValidator).validate(any());

        EarnPointRequest request = EarnPointRequest.builder()
                .memberId("M001")
                .pointKey("PE001")
                .earnTypeCode(EarnTypeCode.NORMAL)
                .earnAmount(5000L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.earn(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.DUPLICATE_POINT_KEY.getMessage());
    }

    @Test
    @DisplayName("Case4 : 포인트 적립 (만료일 범위 초과) - 오류")
    void earn_invalid_expired_days() {
        // given
        doThrow(new PointException(ErrorCode.INVALID_EXPIRED_DAYS))
                .when(pointEarnValidator).validate(any());

        EarnPointRequest request = EarnPointRequest.builder()
                .memberId("M001")
                .pointKey("PE999")
                .earnTypeCode(EarnTypeCode.NORMAL)
                .earnAmount(5000L)
                .expiredDays(9999)
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.earn(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.INVALID_EXPIRED_DAYS.getMessage());
    }

    @Test
    @DisplayName("Case5 : 포인트 적립 (1회 최대 적립 금액 초과) - 오류")
    void earn_exceed_max_earn_amount() {
        // given
        doThrow(new PointException(ErrorCode.EXCEED_MAX_EARN_AMOUNT))
                .when(pointPolicyValidator).validate(anyString(), any());

        EarnPointRequest request = EarnPointRequest.builder()
                .memberId("M001")
                .pointKey("PE999")
                .earnTypeCode(EarnTypeCode.NORMAL)
                .earnAmount(200000L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.earn(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.EXCEED_MAX_EARN_AMOUNT.getMessage());
    }

    @Test
    @DisplayName("Case6 : 포인트 적립 (최대 보유 금액 초과) - 오류")
    void earn_exceed_max_hold_amount() {
        // given
        doThrow(new PointException(ErrorCode.EXCEED_MAX_HOLD_AMOUNT))
                .when(pointPolicyValidator).validate(anyString(), any());

        EarnPointRequest request = EarnPointRequest.builder()
                .memberId("M001")
                .pointKey("PE999")
                .earnTypeCode(EarnTypeCode.NORMAL)
                .earnAmount(5000L)
                .build();

        // when & then
        assertThatThrownBy(() -> pointEarnService.earn(request))
                .isInstanceOf(PointException.class)
                .hasMessage(ErrorCode.EXCEED_MAX_HOLD_AMOUNT.getMessage());
    }

    // ==================== 포인트 적립 취소 ====================

    @Test
    @DisplayName("Case7 : 포인트 적립 취소 - 정상")
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
    @DisplayName("Case8 : 포인트 적립 취소 (존재하지 않는 포인트 키) - 오류")
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
    @DisplayName("Case9 : 포인트 적립 취소 (이미 취소된 포인트) - 오류")
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
    @DisplayName("Case10 : 포인트 적립 취소 (만료된 포인트) - 오류")
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
    @DisplayName("Case11 : 포인트 적립 취소 (사용된 포인트) - 오류")
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
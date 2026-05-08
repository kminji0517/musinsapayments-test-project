package com.example.musinsapayments_test_project.service;

import com.example.musinsapayments_test_project.dto.EarnPointRequest;
import com.example.musinsapayments_test_project.dto.EarnPointResponse;
import com.example.musinsapayments_test_project.enums.EarnStatusCode;
import com.example.musinsapayments_test_project.enums.EarnTypeCode;
import com.example.musinsapayments_test_project.exception.ErrorCode;
import com.example.musinsapayments_test_project.exception.PointException;
import com.example.musinsapayments_test_project.repository.PointEarnRepository;
import com.example.musinsapayments_test_project.validator.PointEarnValidator;
import com.example.musinsapayments_test_project.validator.PointPolicyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointEarnServiceTest {

    @InjectMocks
    private PointEarnService pointEarnService;

    @Mock
    private PointEarnRepository pointEarnRepository;

    @Mock
    private PointEarnValidator pointEarnValidator;

    @Mock
    private PointPolicyValidator pointPolicyValidator;

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
                .when(pointEarnValidator).validate(any());

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
                .when(pointEarnValidator).validate(any());

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
}
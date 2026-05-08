package com.example.musinsapayments_test_project.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode
 * (에러 코드 관리)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),

    // 포인트 정책
    POINT_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 정책을 찾을 수 없습니다."),
    EXCEED_MAX_EARN_AMOUNT(HttpStatus.BAD_REQUEST, "1회 최대 적립 가능 금액을 초과했습니다."),
    EXCEED_MAX_HOLD_AMOUNT(HttpStatus.BAD_REQUEST, "최대 보유 가능 금액을 초과했습니다."),

    // 포인트 적립
    POINT_EARN_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 적립 내역을 찾을 수 없습니다."),
    POINT_EARN_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 적립 내역입니다."),
    POINT_EARN_ALREADY_USED(HttpStatus.BAD_REQUEST, "사용된 포인트는 취소할 수 없습니다."),
    POINT_EARN_ALREADY_EXPIRED(HttpStatus.BAD_REQUEST, "이미 만료된 적립 내역입니다."),
    DUPLICATE_POINT_KEY(HttpStatus.CONFLICT, "이미 존재하는 포인트 키입니다."),
    INVALID_EARN_TYPE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 적립 구분 코드입니다."),
    INVALID_EXPIRED_DAYS(HttpStatus.BAD_REQUEST, "만료일은 1일 이상 5년 미만이어야 합니다."),

    // 포인트 사용
    POINT_USAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 사용 내역을 찾을 수 없습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "포인트 잔액이 부족합니다."),
    EXCEED_REMAINING_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "취소 가능 금액을 초과했습니다."),

    // 서버
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
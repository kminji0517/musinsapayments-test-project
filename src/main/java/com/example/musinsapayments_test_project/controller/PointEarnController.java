package com.example.musinsapayments_test_project.controller;

import com.example.musinsapayments_test_project.dto.CancelEarnRequest;
import com.example.musinsapayments_test_project.dto.CancelEarnResponse;
import com.example.musinsapayments_test_project.dto.EarnPointRequest;
import com.example.musinsapayments_test_project.dto.EarnPointResponse;
import com.example.musinsapayments_test_project.service.PointEarnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PointEarnController
 * (포인트 적립 API)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointEarnController {

    private final PointEarnService pointEarnService;

    /**
     * 포인트 적립
     *
     * @param request 포인트 적립 요청
     * @return 포인트 적립 응답
     */
    @PostMapping("/earn")
    public ResponseEntity<EarnPointResponse> earn(@Valid @RequestBody EarnPointRequest request) {
        return ResponseEntity.ok(pointEarnService.earn(request));
    }

    /**
     * 포인트 적립 취소
     *
     * @param request 포인트 적립 취소 요청
     * @return 포인트 적립 취소 응답
     */
    @PostMapping("/earn/cancel")
    public ResponseEntity<CancelEarnResponse> cancel(@Valid @RequestBody CancelEarnRequest request) {
        return ResponseEntity.ok(pointEarnService.cancel(request));
    }
}
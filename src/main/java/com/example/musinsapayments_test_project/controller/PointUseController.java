package com.example.musinsapayments_test_project.controller;

import com.example.musinsapayments_test_project.dto.UsePointRequest;
import com.example.musinsapayments_test_project.dto.UsePointResponse;
import com.example.musinsapayments_test_project.service.PointUseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PointUseController
 * (포인트 사용 API)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointUseController {

    private final PointUseService pointUseService;

    /**
     * 포인트 사용
     *
     * @param request 포인트 사용 요청
     * @return 포인트 사용 응답
     */
    @PostMapping("/use")
    public ResponseEntity<UsePointResponse> use(@Valid @RequestBody UsePointRequest request) {
        return ResponseEntity.ok(pointUseService.use(request));
    }
}
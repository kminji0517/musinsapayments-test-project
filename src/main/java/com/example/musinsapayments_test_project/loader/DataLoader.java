package com.example.musinsapayments_test_project.loader;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * DataLoader
 * (애플리케이션 실행 시 샘플 데이터 자동 적재)
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final MemberDataLoader memberDataLoader;
    private final PointDataLoader pointDataLoader;

    /**
     * 샘플 데이터 적재 실행
     * member/order_history → point 순서로 적재
     *
     * @param args 애플리케이션 인자
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        memberDataLoader.load();
        pointDataLoader.load();
    }
}
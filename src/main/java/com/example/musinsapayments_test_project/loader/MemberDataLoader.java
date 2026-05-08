package com.example.musinsapayments_test_project.loader;

import com.example.musinsapayments_test_project.domain.Member;
import com.example.musinsapayments_test_project.domain.OrderHistory;
import com.example.musinsapayments_test_project.repository.MemberRepository;
import com.example.musinsapayments_test_project.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MemberDataLoader
 * (회원 및 주문 내역 샘플 데이터 적재)
 */
@Component
@RequiredArgsConstructor
public class MemberDataLoader {

    private final MemberRepository memberRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    /**
     * 회원 3명, 주문 내역 각 5건 적재
     */
    public void load() {
        // 회원 생성
        Member member1 = Member.builder()
                .memberId("M001")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();
        Member member2 = Member.builder()
                .memberId("M002")
                .createdAt(LocalDateTime.of(2024, 3, 15, 0, 0))
                .build();
        Member member3 = Member.builder()
                .memberId("M003")
                .createdAt(LocalDateTime.of(2024, 6, 20, 0, 0))
                .build();
        memberRepository.saveAll(List.of(member1, member2, member3));

        // 주문 내역 생성 (회원별 5건)
        orderHistoryRepository.saveAll(List.of(
                // M001 주문
                OrderHistory.builder().orderId("O001").memberId("M001").createdAt(LocalDateTime.of(2024, 2, 1, 10, 0)).build(),
                OrderHistory.builder().orderId("O002").memberId("M001").createdAt(LocalDateTime.of(2024, 3, 5, 11, 0)).build(),
                OrderHistory.builder().orderId("O003").memberId("M001").createdAt(LocalDateTime.of(2024, 4, 10, 12, 0)).build(),
                OrderHistory.builder().orderId("O004").memberId("M001").createdAt(LocalDateTime.of(2024, 5, 15, 13, 0)).build(),
                OrderHistory.builder().orderId("O005").memberId("M001").createdAt(LocalDateTime.of(2024, 6, 20, 14, 0)).build(),
                // M002 주문
                OrderHistory.builder().orderId("O006").memberId("M002").createdAt(LocalDateTime.of(2024, 4, 1, 10, 0)).build(),
                OrderHistory.builder().orderId("O007").memberId("M002").createdAt(LocalDateTime.of(2024, 5, 10, 11, 0)).build(),
                OrderHistory.builder().orderId("O008").memberId("M002").createdAt(LocalDateTime.of(2024, 6, 15, 12, 0)).build(),
                OrderHistory.builder().orderId("O009").memberId("M002").createdAt(LocalDateTime.of(2024, 7, 20, 13, 0)).build(),
                OrderHistory.builder().orderId("O010").memberId("M002").createdAt(LocalDateTime.of(2024, 8, 25, 14, 0)).build(),
                // M003 주문
                OrderHistory.builder().orderId("O011").memberId("M003").createdAt(LocalDateTime.of(2024, 7, 1, 10, 0)).build(),
                OrderHistory.builder().orderId("O012").memberId("M003").createdAt(LocalDateTime.of(2024, 8, 5, 11, 0)).build(),
                OrderHistory.builder().orderId("O013").memberId("M003").createdAt(LocalDateTime.of(2024, 9, 10, 12, 0)).build(),
                OrderHistory.builder().orderId("O014").memberId("M003").createdAt(LocalDateTime.of(2024, 10, 15, 13, 0)).build(),
                OrderHistory.builder().orderId("O015").memberId("M003").createdAt(LocalDateTime.of(2024, 11, 20, 14, 0)).build()
        ));
    }
}
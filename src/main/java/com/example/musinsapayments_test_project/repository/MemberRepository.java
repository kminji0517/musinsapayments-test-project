package com.example.musinsapayments_test_project.repository;

import com.example.musinsapayments_test_project.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * MemberRepository
 * (회원 데이터 접근)
 */
public interface MemberRepository extends JpaRepository<Member, String> {
}
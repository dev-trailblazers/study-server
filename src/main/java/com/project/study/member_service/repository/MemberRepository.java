package com.project.study.member_service.repository;

import com.project.study.member_service.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsMemberByUsername(String username);
    Optional<Member> findByUsername(String username);

    /** 로그인 시 잠기지 않고, 삭제되지 않은 계정만 조회 */
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.isLocked = false AND m.useYn = true")
    Optional<Member> findAvailableMemberByUsername(String username);

    Optional<Member> findByEmail(String email);
}

package com.project.study.member_service.service;

import com.project.study.member_service.domain.member.Member;
import com.project.study.member_service.dto.MemberDto.JoinDto;
import com.project.study.member_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Member> findAvailableMemberByUsername(String username) {
        return memberRepository.findAvailableMemberByUsername(username);
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }

    public boolean checkDuplicateUsername(String username){
        return memberRepository.existsMemberByUsername(username);
    }

    public void join(JoinDto dto){
        Member entity = dto.toEntity();
        String encodedPassword = passwordEncoder.encode(entity.getPassword());
        entity.setPassword(encodedPassword);

        memberRepository.save(entity);
    }
}
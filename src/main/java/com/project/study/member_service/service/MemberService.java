package com.project.study.member_service.service;

import com.project.study.member_service.domain.member.Member;
import com.project.study.member_service.domain.member.MemberDto;
import com.project.study.member_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Member> findAvailableMemberByUsername(String username) {
        return memberRepository.findAvailableMemberByUsername(username);
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    @Transactional(readOnly = true)
    public boolean checkDuplicateUsername(String username) {
        return memberRepository.existsMemberByUsername(username);
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }


    public Member join(MemberDto.JoinDto dto) {
        memberRepository.findByEmail(dto.getEmail())
                .ifPresent(o -> {
                    throw new IllegalArgumentException("이미 가입된 이메일입니다.");
                });
        Member entity = dto.toEntity();
        String encodedPassword = passwordEncoder.encode(entity.getPassword());
        entity.setPassword(encodedPassword);

        return memberRepository.save(entity);
    }
}

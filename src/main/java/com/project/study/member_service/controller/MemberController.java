package com.project.study.member_service.controller;


import com.project.study.member_service.domain.email.EmailVerify;
import com.project.study.member_service.dto.MemberDto.JoinDto;
import com.project.study.authentication_service.service.EmailService;
import com.project.study.member_service.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@RestController
public class MemberController {
    private final MemberService memberService;
    private final EmailService emailService;


    @PostMapping("/join/username")
    public ResponseEntity<Boolean> duplicationCheckForUsername(@RequestBody String username){
        return ResponseEntity.ok(memberService.checkDuplicateUsername(username));
    }

    @PostMapping("/join")
    public void joinMember(@RequestBody @Valid JoinDto dto){
        if(memberService.checkDuplicateUsername(dto.getUsername())){
            throw new IllegalArgumentException("아이디는 중복될 수 없습니다.");     //todo: controller advice로 예외처리
        }
        Optional<EmailVerify> auth = emailService.findByEmail(dto.getEmail());
        if(auth.isEmpty() || !auth.get().isStatus()){
            throw new IllegalArgumentException("이메일 인증 후 시도해주세요.");
        }
        memberService.join(dto);
    }
}

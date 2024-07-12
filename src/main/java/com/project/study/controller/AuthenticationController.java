package com.project.study.controller;

import com.project.study.domain.email.EmailAuthentication;
import com.project.study.domain.email.EmailAuthenticationDto;
import com.project.study.domain.member.dto.MemberJoinDto;
import com.project.study.security.jwt.JwtService;
import com.project.study.service.EmailService;
import com.project.study.service.MemberService;
import com.project.study.validation.MemberEmail;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final EmailService emailService;
    private final MemberService memberService;


    @PostMapping("/username")
    public ResponseEntity<Boolean> duplicationCheckForUsername(@RequestBody String username){
        return ResponseEntity.ok(memberService.checkDuplicateUsername(username));
    }

    @PostMapping("/new/email")
    public void requestVerificationCodeForEmail(@RequestBody @MemberEmail String email){
        emailService.sendVerificationCode(email);
    }

    @PostMapping("/email")
    public ResponseEntity<Boolean> checkVerificationCodeForEmail(@RequestBody @Valid EmailAuthenticationDto dto){
        if(emailService.checkVerificationCodeForEmail(dto)){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.badRequest().body(false);
    }

    @PostMapping("/join")
    public void joinMember(@RequestBody @Valid MemberJoinDto dto){
        if(memberService.checkDuplicateUsername(dto.username())){
            throw new IllegalArgumentException("아이디는 중복될 수 없습니다.");
        }
        Optional<EmailAuthentication> auth = emailService.findByEmail(dto.email());
        if(auth.isEmpty() || !auth.get().isStatus()){
            throw new IllegalArgumentException("이메일 인증 후 시도해주세요.");
        }

        memberService.join(dto);
    }


    @PostMapping("/reissue")
    public void reissueToken(HttpServletRequest request, HttpServletResponse response){
        try {
            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("refreshToken"))
                    .findFirst().orElse(null)
                    .getValue();

            Map<String, String> tokens = jwtService.reissuanceToken(refreshToken);

            Cookie cookie = new Cookie("refreshToken", tokens.get("refreshToken"));
            cookie.setPath("/");
            cookie.setMaxAge((int) jwtService.getREFRESH_TOKEN_EXPIRED_TIME_MS());
            cookie.setHttpOnly(true);

            response.addHeader("Authorization", "Bearer " + tokens.get("accessToken"));
            response.addCookie(cookie);

            if(refreshToken == null){
                throw new IllegalArgumentException("리프레시 토큰이 존재하지 않습니다.");
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("쿠키가 존재하지 않습니다.");
        }
    }

    @GetMapping("/health")
    public String health(){
        return "health check!";
    }

}

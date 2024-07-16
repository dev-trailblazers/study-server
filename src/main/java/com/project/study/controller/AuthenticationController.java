package com.project.study.controller;

import com.project.study.domain.email.EmailAuthentication;
import com.project.study.domain.email.EmailAuthenticationDto;
import com.project.study.domain.member.dto.MemberJoinDto;
import com.project.study.security.jwt.JwtService;
import com.project.study.security.jwt.TokenType;
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

import static com.project.study.security.jwt.TokenType.ACCESS_TOKEN;
import static com.project.study.security.jwt.TokenType.REFRESH_TOKEN;


@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final EmailService emailService;
    private final MemberService memberService;


    @GetMapping("/health")
    public String health(){
        return "health check!";
    }

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
        boolean result = emailService.checkVerificationCodeForEmail(dto);
        return ResponseEntity.ok(result);  //인증 실패도 응답 자체는 성공이기 때문에 200을 반환
    }

    @PostMapping("/join")
    public void joinMember(@RequestBody @Valid MemberJoinDto dto){
        if(memberService.checkDuplicateUsername(dto.username())){
            throw new IllegalArgumentException("아이디는 중복될 수 없습니다.");     //todo: controller advice로 예외처리
        }
        Optional<EmailAuthentication> auth = emailService.findByEmail(dto.email());
        if(auth.isEmpty() || !auth.get().isStatus()){
            throw new IllegalArgumentException("이메일 인증 후 시도해주세요.");
        }
        memberService.join(dto);
    }


    @PostMapping("/reissue")
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            throw new IllegalArgumentException("쿠키가 존재하지 않습니다.");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN))
                .findFirst().orElse(null)
                .getValue();
        if (refreshToken == null) {
            throw new IllegalArgumentException("리프레시 토큰이 존재하지 않습니다.");
        }

        Map<TokenType, String> tokens = jwtService.reissuanceToken(refreshToken);

        Cookie cookie = new Cookie(REFRESH_TOKEN.name(), tokens.get(REFRESH_TOKEN));
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtService.getREFRESH_TOKEN_EXPIRED_TIME_MS());
        cookie.setHttpOnly(true);

        response.addHeader("Authorization", "Bearer " + tokens.get(ACCESS_TOKEN));
        response.addCookie(cookie);
    }

}

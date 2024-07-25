package com.project.study.authentication_service.controller;

import com.project.study.authentication_service.service.AuthenticationService;
import com.project.study.authentication_service.domain.jwt.TokenType;
import com.project.study.member_service.domain.email.EmailVerifyDto;
import com.project.study.member_service.domain.validation.MemberEmail;
import com.project.study.authentication_service.service.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

import static com.project.study.authentication_service.domain.jwt.TokenType.ACCESS_TOKEN;
import static com.project.study.authentication_service.domain.jwt.TokenType.REFRESH_TOKEN;


@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final EmailService emailService;

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

        Map<TokenType, String> tokens = authenticationService.reissuanceToken(refreshToken);

        Cookie cookie = new Cookie(REFRESH_TOKEN.name(), tokens.get(REFRESH_TOKEN));
        cookie.setPath("/");
        cookie.setMaxAge((int) authenticationService.getREFRESH_TOKEN_EXPIRED_TIME_MS());
        cookie.setHttpOnly(true);

        response.addHeader("Authorization", "Bearer " + tokens.get(ACCESS_TOKEN));
        response.addCookie(cookie);
    }

    @PostMapping("/new/email")
    public void requestVerificationCodeForEmail(@RequestBody @MemberEmail String email){
        emailService.sendVerificationCode(email);
    }

    @PostMapping("/verify/email")
    public ResponseEntity<Boolean> checkVerificationCodeForEmail(@RequestBody @Valid EmailVerifyDto dto){
        boolean result = emailService.checkVerificationCodeForEmail(dto);
        return ResponseEntity.ok(result);  //인증 실패도 응답 자체는 성공이기 때문에 HTTP 상태 코드는 200을 반환
    }
}

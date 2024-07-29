package com.project.study.authentication_service.domain.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {
    @Value("${spring.security.jwt.secret}")
    private String secret;
    private SecretKey secretKey;

    private final ObjectMapper objectMapper;


    @PostConstruct
    public void init() {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }


    /** 토큰 유효성 검증 */
    public void validate(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(Jwts.header().build(), Jwts.claims().build(), "토큰이 만료되었습니다.");
        } catch (JwtException e) {
            throw new JwtException("토큰이 유효하지 않습니다.");
        }
    }


    /** 토큰 발행
     * @Param claims : 토큰에 담을 정보
     * @Param expiredTimeMs : 토큰 만료 시간
     * */
    public String issueToken(Map<String, Object> claims, long expiredTimeMs) {
        JwtBuilder builder = Jwts.builder()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredTimeMs))
                .signWith(secretKey);

        for(Map.Entry<String, Object> entry : claims.entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }
        return builder.compact();
    }

    /** 토큰 값 조회
     * @Param token : 토큰 값
     * @Param key : 조회할 값의 키
     * @Param type : 조회할 값의 타입
     * */
    public <T> T getClaimInToken(String token, String key, Class<T> type) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return objectMapper.convertValue(claims.get(key), type);
    }


    /** 토큰의 남은 만료시간을 가져온다. */
    public long getRemainingTime(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .getTime() - System.currentTimeMillis();
    }

}

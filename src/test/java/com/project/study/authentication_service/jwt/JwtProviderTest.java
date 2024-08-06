package com.project.study.authentication_service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.study.authentication_service.domain.jwt.JwtProvider;
import com.project.study.member_service.domain.member.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JwtProviderTest {
    JwtProvider jwtProvider = new JwtProvider(new ObjectMapper());
    final long accessTokenExpiration = 100_000L;
    final long refreshTokenExpiration = 1_000_000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtProvider, "secret", "this_is_a_very_long_test_secret_key_1234");
        jwtProvider.init();
    }

    @DisplayName("토큰 생성 - [성공]")
    @Test
    void generate_token() {
        String token = jwtProvider.issueToken(Map.of("member", new Member()), accessTokenExpiration);
        System.out.println(token);
        assertNotNull(token);
    }


}
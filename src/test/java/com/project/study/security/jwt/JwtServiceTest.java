package com.project.study.security.jwt;

import com.project.study.domain.member.JoinPlatform;
import com.project.study.domain.member.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class JwtServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIssueAccessToken() {
        // Given
        Member member = createDummyMember();

        // Mock JWT provider behavior
        when(jwtProvider.issueToken(anyMap(), anyLong())).thenReturn("dummy_access_token");

        // When
        String accessToken = jwtService.issueAccessToken(member);

        // Then
        assertNotNull(accessToken);
        assertEquals("dummy_access_token", accessToken);
    }

    @Test
    public void testSaveAndRetrieveOAuth2Token() {
        // Given
        String username = "testUser";
        OAuth2AccessToken token = createDummyOAuth2Token();

        // Mock RedisTemplate behavior
        doNothing().when(redisTemplate).opsForValue().set(anyString(), any(), anyLong());

        // When
        jwtService.saveOAuth2Token(username, token);

        // Then
        verify(redisTemplate, times(1)).opsForValue().set(anyString(), any(), anyLong());
    }

    private Member createDummyMember() {
        return Member.builder()
                .id(1L)
                .username("testuser")
                .name("tester")
                .joinPlatform(JoinPlatform.BASIC)
                .build();
    }

    private OAuth2AccessToken createDummyOAuth2Token() {
        OAuth2AccessToken dummyAccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "dummy_access_token",
                Instant.now(),
                Instant.now().plusSeconds(3600));
        return dummyAccessToken;
    }
}

package com.project.study.security.jwt;

import com.project.study.domain.member.JoinPlatform;
import com.project.study.domain.member.Member;
import com.project.study.repository.MemberRepository;
import com.project.study.security.model.CustomUserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtService {
    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    @Value("${jwt.expired_time.access_token}")
    private long ACCESS_TOKEN_EXPIRED_TIME_MS;

    @Getter
    @Value("${jwt.expired_time.refresh_token}")
    private long REFRESH_TOKEN_EXPIRED_TIME_MS;

    private static final String BLACK_LIST_PREFIX = "restricted_";
    private static final String OAUTH2_PREFIX = "oauth2_";


    /**
     * 엑세스 토큰을 검증하고 Redis에서 블랙리스트 체크를 진행한다.
     */
    public boolean validateAccessToken(String token) {
        if(!jwtProvider.validate(token)){
            return false;
        }
        if(redisTemplate.hasKey(BLACK_LIST_PREFIX + token)){
            log.warn("사용이 제한된 토큰입니다.");
            return false;
        }
        return true;
    }

    /**
     * OAuth2 로그아웃에 사용하기 위해 OAuth2에서 발급받은 액세스 토큰을 저장
     */
    public void saveOAuth2Token(String username, OAuth2AccessToken token) {
        long expiredTimeMs = token.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();
        String key = OAUTH2_PREFIX + username;    //ex. oauth2_kakao123456
        redisTemplate.opsForValue().set(key, token.getTokenValue(), expiredTimeMs, TimeUnit.MILLISECONDS);
    }

    /** OAuth2 엑세스 토큰 조회 */
    public String getOauth2AccessToken(String username){
        return (String) redisTemplate.opsForValue().get(OAUTH2_PREFIX + username);
    }

    /**
     * 액세스 토큰 발행
     */
    public String issueAccessToken(Member member) {
        Map<String, Object> claims = Map.of(
                "id", member.getId(),
                "member", JwtMemberInfo.from(member),
                "joinPlatform", member.getJoinPlatform(),
                "role", member.getRole()
        );
        return jwtProvider.issueToken(claims, ACCESS_TOKEN_EXPIRED_TIME_MS);
    }

    /**
     * 리프레시 토큰 발행
     *
     * 화이트 리스트로 토큰을 관리하기 위해 Redis에 토큰을 저장한다.
     * 이때 각 기기마다 다른 리프레시 토큰을 발급 받을 수 있도록 키를 토큰으로 저장한다.
     */
    public String issueRefreshToken(Long memberId) {
        String refreshToken = jwtProvider.issueToken(Map.of("id", memberId), REFRESH_TOKEN_EXPIRED_TIME_MS);

        redisTemplate.opsForValue().set(refreshToken, memberId, REFRESH_TOKEN_EXPIRED_TIME_MS, TimeUnit.MILLISECONDS);
        return refreshToken;
    }

    /**
     * 토큰 재발급
     *
     * Redis에서 TTL을 사용하기 때문에 만료 체크가 불필요하고, 일치해야하기 때문에 유효한 토큰일 수 밖에 없다.
     * 따라서 별도의 검증을 하지않는다.
     *
     * 기존 토큰을 삭제하고 새로운 리프레시 토큰과 액세스 토큰을 발급한다.
     */
    public Map<String, String> reissuanceToken(String refreshToken) {
        if (!redisTemplate.hasKey(refreshToken)) {
            throw new IllegalArgumentException("사용할 수 없는 리프레시 토큰입니다.");
        }

        redisTemplate.delete(refreshToken);

        Long memberId = jwtProvider.getClaimInToken(refreshToken, "id", Long.class);
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다.")
        );

        String issuedRefreshToken = issueRefreshToken(memberId);
        String accessToken = issueAccessToken(member);

        return Map.of(
                "refreshToken", issuedRefreshToken,
                "accessToken", accessToken
        );
    }

    /** 토큰 삭제 */
    public void removeToken(String key) {
        redisTemplate.delete(key);
    }


    /** 토큰을 사용하지 못하도록 블랙리스트에 추가
     * 남은 만료 기간 동안 저장한다.
     * */
    public void restrictToken(String token) {
        JwtMemberInfo member = jwtProvider.getClaimInToken(token, "member", JwtMemberInfo.class);
        long remainingTime = jwtProvider.getRemainingTime(token);
        redisTemplate.opsForValue().set(BLACK_LIST_PREFIX + token, member.id, remainingTime, TimeUnit.MILLISECONDS);
    }


    /**
     * 토큰에서 회원 인증 정보를 가져온다.
     */
    public CustomUserDetails getUserDetailsInToken(String token) {
        Member member = jwtProvider.getClaimInToken(token, "member", JwtMemberInfo.class).to();
        return new CustomUserDetails(member);
    }

    public JoinPlatform getJoinPlatformInToken(String token) {
        return jwtProvider.getClaimInToken(token, "joinPlatform", JoinPlatform.class);
    }


    /**
     * JWT에 저장하기 위한 멤버 객체
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class JwtMemberInfo {
        private Long id;
        private String name;
        private String profile_image;
        private Member.RoleType roleType;

        protected Member to() {
            return Member.builder()
                    .id(this.id)
                    .name(this.name)
                    .profile_image(this.profile_image)
                    .role(this.roleType)
                    .build();
        }

        protected static JwtMemberInfo from(Member member) {
            return new JwtMemberInfo(
                    member.getId(),
                    member.getName(),
                    member.getProfile_image(),
                    member.getRole()
            );
        }
    }

    /**
     * 토큰 타입을 구분하기 위한 ENUM
     */
    public enum TokenType {
        ACCESS_TOKEN, REFRESH_TOKEN, OAuth2_TOKEN
    }
}

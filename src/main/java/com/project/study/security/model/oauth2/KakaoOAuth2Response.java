package com.project.study.security.model.oauth2;

import com.project.study.domain.member.Member;

import java.time.LocalDate;
import java.util.Map;

import static com.project.study.domain.member.JoinPlatform.KAKAO;

public record KakaoOAuth2Response(
        Long id,
        KakaoAccount kakaoAccount
) implements OAuth2Response {
    public record KakaoAccount(
            String name,
            LocalDate birth,
            Member.Gender gender
    ) {
//        public static KakaoAccount from(Map<String, Object> attributes) {
//            return new KakaoAccount(
//                    String.valueOf(attributes.get("name")),
//                    LocalDate.of(
//                            Integer.parseInt(String.valueOf(attributes.get("birthyear"))),
//                            Integer.parseInt(String.valueOf(attributes.get("birthday")).substring(0, 2)),
//                            Integer.parseInt(String.valueOf(attributes.get("birthday")).substring(2))
//                    ),
//                    String.valueOf(attributes.get("gender")).toLowerCase().equals("male") ? 'M' : 'F'
//            );
//        }

        //todo: 카카오 사업자 등록 전까지만 사용
        private static KakaoAccount from(Map<String, Object> attributes) {
            return new KakaoAccount("홍길동", LocalDate.of(1999, 11, 23), Member.Gender.M);
        }
    }


    @Override
    public Member toMember() {
        String providerId = String.valueOf(id);
        String username = KAKAO.getValue() + "_" + providerId;

        return Member.builder()
                .username(username)
                .name(kakaoAccount().name())
                .email(username + "@kakao.com") //임시 이메일
                .birth(kakaoAccount().birth())
                .gender(kakaoAccount().gender())
                .role(Member.RoleType.ROLE_USER)
                .joinPlatform(KAKAO)
                .build();
    }

    public static KakaoOAuth2Response from(Map<String, Object> attributes) {
        return new KakaoOAuth2Response(
                Long.valueOf(String.valueOf(attributes.get("id"))),
                KakaoAccount.from((Map<String, Object>) attributes.get("kakao_account"))
        );
    }
}
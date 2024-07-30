package com.project.study.member_service.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JoinPlatform {
    BASIC("basic", "none"),
    KAKAO("kakao", "https://kapi.kakao.com/v1/user/logout");

    private final String registrationId;
    private final String logoutUrl;


    public static JoinPlatform of(String value) {
        for (JoinPlatform joinPlatform : JoinPlatform.values()) {
            if (joinPlatform.getRegistrationId().equals(value)) {
                return joinPlatform;
            }
        }
        throw new IllegalArgumentException("해당하는 값이 존재하지 않습니다.");
    }
}

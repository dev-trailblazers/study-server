package com.project.study.member_service.domain.member;

import lombok.Getter;

@Getter
public enum JoinPlatform {
    BASIC("basic", "none"),
    KAKAO("kakao", "https://kapi.kakao.com/v1/user/logout");

    private final String value;
    private final String logoutUrl;

    JoinPlatform(String value, String logoutUrl) {
        this.value = value;
        this.logoutUrl = logoutUrl;
    }

    public static JoinPlatform of(String value) {
        for (JoinPlatform joinPlatform : JoinPlatform.values()) {
            if (joinPlatform.getValue().equals(value)) {
                return joinPlatform;
            }
        }
        throw new IllegalArgumentException("해당하는 값이 존재하지 않습니다.");
    }
}

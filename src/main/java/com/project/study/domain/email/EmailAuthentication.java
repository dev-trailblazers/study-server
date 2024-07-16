package com.project.study.domain.email;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * RedisTemplate에서 byte[]로 직렬화를 진행하도록 선언했기 때문에
 * Serializable을 구현한 객체만 저장이 가능하다.
 */
@Getter
@NoArgsConstructor
public class EmailAuthentication implements Serializable {
    private static final long serialVersionUID = 362498820763181265L;

    private String email;
    private String code;
    @Setter
    private boolean status = false;

    public EmailAuthentication(String email, String code) {
        this.email = email;
        this.code = code;
    }
}

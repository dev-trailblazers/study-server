package com.project.study.domain.email;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * RedisTemplate에 byte[]로
 */
@Getter
@NoArgsConstructor
public class EmailAuthentication implements Serializable {
    private static final long serialVersionUID = 362498820763181265L;

    private String email;
    private String code;
    @Setter private boolean status = false;

    public EmailAuthentication(String email, String code) {
        this.email = email;
        this.code = code;
    }
}

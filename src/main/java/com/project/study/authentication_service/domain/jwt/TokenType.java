package com.project.study.authentication_service.domain.jwt;

/**
 * 용도에 따라 토큰을 구분하기 위한 ENUM
 */
public enum TokenType {
    ACCESS_TOKEN, REFRESH_TOKEN, OAUTH2_TOKEN
}
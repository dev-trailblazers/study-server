package com.project.study.authentication_service.domain.user.oauth2;


import com.project.study.member_service.domain.member.Member;

public interface OAuth2Response {
    Member toMember();
}

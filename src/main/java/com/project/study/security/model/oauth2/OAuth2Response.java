package com.project.study.security.model.oauth2;

import com.project.study.domain.member.Member;

public interface OAuth2Response {
    Member toMember();
}

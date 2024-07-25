package com.project.study.authentication_service.domain.jwt;

import com.project.study.member_service.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JWT Payload에 담아두고 사용할 Member 정보를 담은 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInJWT {
    private Long id;
    private String name;
    private String profile_image;
    private Member.RoleType roleType;

    public Member to() {
        return Member.builder()
                .id(this.id)
                .name(this.name)
                .profile_image(this.profile_image)
                .role(this.roleType)
                .build();
    }

    public static MemberInJWT from(Member member) {
        return new MemberInJWT(
                member.getId(),
                member.getName(),
                member.getProfile_image(),
                member.getRole()
        );
    }
}

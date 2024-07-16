package com.project.study.security.jwt;

import com.project.study.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JWT에 저장하기 위한 멤버 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JwtMemberInfo {
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

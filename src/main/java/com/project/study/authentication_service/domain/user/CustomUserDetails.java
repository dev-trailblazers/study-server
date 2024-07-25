package com.project.study.authentication_service.domain.user;

import com.project.study.member_service.domain.member.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomUserDetails implements OAuth2User, UserDetails {
    @Getter
    private final Member member;

    public Long getId(){
        return member.getId();
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getName() {
        return member.getName();
    }

    /**
     * OAuth2로부터 받아온 유저 정보 ex) profile
     * customOAuth2UserService에서 OAuth2 DTO 객체를 따로 만들어서 처리하기 때문에 사용하지 않음
     * */
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> member.getRole().name());
        return authorities;
    }


    @Override
    public boolean isAccountNonExpired() {
        return member.isUseYn();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !member.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !member.isLocked();
    }

    @Override
    public boolean isEnabled() {
        return member.isUseYn() && !member.isLocked();
    }
}

package com.project.study.domain.member.dto;

import com.project.study.domain.member.JoinPlatform;
import com.project.study.domain.member.Member;
import com.project.study.validation.MemberEmail;
import com.project.study.validation.Password;
import com.project.study.validation.Username;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
public record MemberJoinDto(
        @Username
        String username,
        @Password
        String password,
        @MemberEmail
        String email,
        @NotBlank
        @Pattern(regexp = "^[가-힣]{2,6}$")
        String name,
        @NotNull
        @Past(message = "생년월일은 오늘 이전 날짜여야 합니다.")
        LocalDate birth,
        @NotNull
        Member.Gender gender,
        Member.RoleType role,
        JoinPlatform joinPlatform

) implements Serializable {

    public Member toEntity() {
        return Member.builder()
                .username(username)
                .password(password)
                .email(email)
                .name(name)
                .birth(birth)
                .gender(gender)
                .role(role)
                .joinPlatform(joinPlatform)
                .modifiedBy(0L)
                .build();
    }
}
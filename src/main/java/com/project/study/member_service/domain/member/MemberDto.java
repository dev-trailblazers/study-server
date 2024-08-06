package com.project.study.member_service.domain.member;

import com.project.study.member_service.domain.member.Member.Gender;
import com.project.study.member_service.domain.member.Member.RoleType;
import com.project.study.member_service.domain.validation.MemberEmail;
import com.project.study.member_service.domain.validation.Password;
import com.project.study.member_service.domain.validation.Username;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private LocalDate birth;
    private Gender gender;
    private String profileImage;

    public static MemberDto fromEntity(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .name(member.getName())
                .birth(member.getBirth())
                .gender(member.getGender())
                .profileImage(member.getProfile_image())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JoinDto {
        @Username
        private String username;

        @Password(message = "비밀번호는 영문, 숫자, 특수 문자를 하나씩 포함한 8~16자리입니다.")
        private String password;

        @MemberEmail
        private String email;

        @NotBlank
        @Pattern(regexp = "^[가-힣]{2,6}$")
        private String name;

        @Past(message = "생년월일은 오늘 이전 날짜여야 합니다.")
        private LocalDate birth;

        @NotNull
        private Gender gender;
        private String profileImage;

        private JoinPlatform joinPlatform;


        public static JoinDto fromEntity(Member member) {
            return JoinDto.builder()
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .email(member.getEmail())
                    .name(member.getName())
                    .birth(member.getBirth())
                    .gender(member.getGender())
                    .profileImage(member.getProfile_image())
                    .joinPlatform(member.getJoinPlatform())
                    .build();
        }

        public Member toEntity() {
            JoinPlatform joinPlatform = this.joinPlatform == null ? JoinPlatform.BASIC : this.joinPlatform;
            return Member.builder()
                    .username(this.username)
                    .password(this.password)
                    .email(this.email)
                    .name(this.name)
                    .birth(this.birth)
                    .gender(this.gender)
                    .profile_image(this.profileImage)
                    .joinPlatform(joinPlatform)
                    .role(RoleType.ROLE_USER) // Default role for new members
                    .isLocked(false)
                    .useYn(true)
                    .build();
        }
    }
}

package com.project.study.member_service.domain.email;

import com.project.study.member_service.domain.validation.MemberEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record EmailVerifyDto(
        @MemberEmail
        String email,

        @NotBlank
        @Length(min = 6, max = 6)
        String code
) {
}

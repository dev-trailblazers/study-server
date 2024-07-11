package com.project.study.domain.email;

import com.project.study.validation.MemberEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record EmailAuthenticationDto(
        @MemberEmail String email,
        @NotBlank
        @Length(min = 6, max = 6) String code
) {
}

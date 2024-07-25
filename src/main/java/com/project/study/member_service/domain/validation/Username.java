package com.project.study.member_service.domain.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = { })
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@NotBlank
@Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])[a-z0-9]{3,16}$")
public @interface Username {
    String message() default "아이디는 영소문자와 숫자를 하나씩 포함한 3~16자리입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

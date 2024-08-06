package com.project.study.global.config;

import com.project.study.authentication_service.domain.user.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if(principal instanceof CustomUserDetails) {
                    return Optional.of(((CustomUserDetails) principal).getId());
                }
            }
            return null;        //인증 정보가 없을 때 null이 아닌 임시 값을 사용하면 인증 정보 값을 직접 주입해도 덮어쓰기 됨
        };
    }
}
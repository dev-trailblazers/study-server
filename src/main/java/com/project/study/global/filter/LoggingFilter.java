package com.project.study.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        log.info("Request URI: {}", request.getRequestURI());

        //요청 처리 후 다음 필터로 넘겨서 다른 필터를 모두 처리하고 돌아오면 응답에 대한 처리를 진행한다.
        chain.doFilter(request, response);

        int status = response.getStatus();
        if (status >= 400) {
            log.warn("Response Status: {}", status);
        } else {
            log.info("Response Status: {}", status);
        }
    }
}
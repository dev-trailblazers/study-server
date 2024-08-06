package com.project.study.authentication_service.config;


import com.project.study.authentication_service.domain.user.CustomUserDetails;
import com.project.study.authentication_service.domain.user.oauth2.KakaoOAuth2Response;
import com.project.study.authentication_service.domain.user.oauth2.OAuth2Response;
import com.project.study.authentication_service.service.AuthenticationService;
import com.project.study.authentication_service.service.OAuth2Service;
import com.project.study.global.filter.LoggingFilter;
import com.project.study.member_service.domain.member.JoinPlatform;
import com.project.study.member_service.domain.member.Member;
import com.project.study.member_service.domain.member.MemberDto;
import com.project.study.member_service.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static com.project.study.authentication_service.domain.jwt.TokenType.REFRESH_TOKEN;
import static com.project.study.member_service.domain.member.JoinPlatform.KAKAO;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationService authenticationService;
    private final MemberService memberService;
    private final OAuth2Service oAuth2Service;

    @Value("${client.url}")
    private String CLIENT_URL;

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/error/**", "/v3/api-docs/**", "/swagger-ui/**",
            "/login/**", "/logout/**",
            "/api/v1/auth/**",
            "/api/v1/member/join/**",
            "/api/v1/study/detail/**",
            "/api/v1/recruitment/list/**"
    );

    @Bean
    public SecurityFilterChain config(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        return http
                .csrf(auth -> auth.disable())
                .cors(cors -> cors.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(EXCLUDED_PATHS.toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
                //OAuth2 Login Request URL Pattern = {baseUrl}/oauth2/authorization/{provider}
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(user -> user.userService(
                                customOAuth2UserService(passwordEncoder))
                        ).successHandler(customOAuth2LoginSuccessHandler())
                        .failureHandler(customAuthenticationFailureHandler())
                )
                .addFilterBefore(new LoggingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new TokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(
                        new CustomAuthenticationFilter(authenticationManager(authenticationConfiguration)),
                        UsernamePasswordAuthenticationFilter.class
                )
                .logout(logout -> logout
                        .addLogoutHandler(customLogoutHandler())
                        .deleteCookies(REFRESH_TOKEN.name())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            //로그아웃 후 리다이렉트를 비활성화 하기 위해 200 상태코드를 반환하고 끝내도록 커스텀
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                )
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) -> response
                                .sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage())
                        )
                )
                .build();
    }


    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler() {
        return new CustomOAuth2LoginSuccessHandler();
    }

    @Bean
    CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    CustomLogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler();
    }


    /**
     * UsernamePasswordAuthenticationFilter의 AuthenticationManager 호출 시
     * AuthenticationProvider에서 UserDetailsService를 호출한 다음 비밀번호를 확인한다.
     */
    @Bean
    public UserDetailsService customUserDetailsService() {
        return username -> memberService.findAvailableMemberByUsername(username)
                .map(member -> new CustomUserDetails(member))
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다."));
    }


    /**
     * userInfoEndPoint에서 OAuth2 유저 정보를 가져오는 로직을 담당한다.
     */
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService(PasswordEncoder passwordEncoder) {
        final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return userRequest -> {
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2Response oAuth2Response;

            //OAuth2 platform이 추가되면 해당 OAuth2Response를 구현한 후 조건문을 추가하면 된다.
            if (registrationId.equals(KAKAO.getRegistrationId())) {
                oAuth2Response = KakaoOAuth2Response.from(oAuth2User.getAttributes());
            } else {
                throw new IllegalArgumentException("지원하지 않는 OAuth2 플랫폼입니다.");
            }

            Member member = oAuth2Response.toMember();
            MemberDto.JoinDto joinDto = MemberDto.JoinDto.fromEntity(member);

            //로그아웃 시 OAuth2에서도 로그아웃 할 것이기 때문에 OAuth2AccessToken을 저장한다.
            OAuth2AccessToken accessToken = userRequest.getAccessToken();
            authenticationService.saveOAuth2Token(member.getUsername(), accessToken);

            /* customOAuth2UserService는 유저 정보를 조회하는 로직이지만, oauth2의 경우 여기서 이미 인증이 된 것이기 때문에
             * 유저가 DB에 없는 경우 자동 회원가입을 진행한다.
             */
            return memberService.findAvailableMemberByUsername(member.getUsername())
                    .map(CustomUserDetails::new)
                    .orElseGet(() -> new CustomUserDetails(memberService.join(joinDto)));
        };
    }


    private class TokenAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            //인증 대상에서 제외된 URL 인증 스킵
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            String path = request.getRequestURI();
            boolean isExcluded = EXCLUDED_PATHS.stream()
                    .anyMatch(excludedPath -> antPathMatcher.match(excludedPath, path));
            if (isExcluded) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 꺼내기
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                log.warn("잘못된 토큰 헤더 : {}", authorization);
                filterChain.doFilter(request, response);
                return;
            }
            String token = authorization.split(" ")[1];

            //토큰 유효성 검증
            authenticationService.validateAccessToken(token);

            //토큰이 유효하면 UserDetails 객체를 만들어서 SecurityContext의 Authentication으로 넣어준다.
            UserDetails userDetails = authenticationService.getUserDetailsInToken(token);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        }
    }

    @RequiredArgsConstructor
    private class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
        private final AuthenticationManager authenticationManager;

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
                throws AuthenticationException {
            String username = obtainUsername(request);
            String password = obtainPassword(request);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        }

        @Override
        protected void successfulAuthentication(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain chain,
                                                Authentication auth) {
            CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
            issueTokens(response, customUserDetails.getMember());
        }

    }


    private class CustomOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication) throws IOException {
            super.clearAuthenticationAttributes(request);
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            issueTokens(response, customUserDetails.getMember());

            response.sendRedirect(CLIENT_URL);
        }
    }

    private class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationException exception) throws IOException {
            String errorMessage = "";
            if (exception instanceof BadCredentialsException) {
                errorMessage = "아이디 또는 비밀번호가 맞지 않습니다. 다시 확인해주세요.";
            } else if (exception instanceof InternalAuthenticationServiceException) {
                errorMessage = "내부 시스템 문제로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요. ";
            } else if (exception instanceof UsernameNotFoundException) {
                errorMessage = "존재하지 않는 계정입니다. 회원가입 후 로그인해주세요.";
            } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
                errorMessage = "인증 요청이 거부되었습니다. 관리자에게 문의하세요.";
            } else {
                errorMessage = "알 수 없는 오류로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요.";
            }
            log.warn(errorMessage);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(errorMessage);
            response.getWriter().flush();
        }
    }

    private class CustomLogoutHandler implements LogoutHandler {
        @Override
        public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
            SecurityContextHolder.clearContext();

            //로그아웃 필터 이전에 인증 필터에서 토큰 검증을 진행했기 때문에 여기선 생략한다.
            String authorization = request.getHeader("Authorization");
            String accessToken = authorization.split(" ")[1];

            /*
             * accessToken 블랙리스트에 추가
             * refreshToken이 쿠키에 존재한다면, 화이트리스트에서 삭제
             * */
            authenticationService.restrictToken(accessToken);
            Optional.ofNullable(request.getCookies())
                    .ifPresent((cookies) -> Arrays.stream(cookies)
                            .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN.name()))
                            .findFirst()
                            .ifPresent((cookie) -> authenticationService.removeToken(cookie.getValue()))
                    );
            log.debug("토큰 비활성화 완료");

            //OAuth2 로그인 사용자가 로그아웃한 경우 해당 플랫폼에도 로그아웃 요청
            JoinPlatform joinPlatform = authenticationService.getJoinPlatformInToken(accessToken);

            if (joinPlatform == null || joinPlatform == JoinPlatform.BASIC) return;

            Long memberId = authenticationService.getUserDetailsInToken(accessToken).getMember().getId();
            String username = memberService.findById(memberId).getUsername();

            String oauth2AccessToken = authenticationService.getOauth2AccessToken(username);
            oAuth2Service.logout(oauth2AccessToken, joinPlatform);
            log.debug("OAuth2에서 로그아웃 완료");
        }

    }


    private void issueTokens(HttpServletResponse response, Member member) {
        String accessToken = authenticationService.issueAccessToken(member);
        String refreshToken = authenticationService.issueRefreshToken(member.getId());

        Cookie cookie = new Cookie(REFRESH_TOKEN.name(), refreshToken);
        cookie.setPath("/");
        cookie.setMaxAge((int) authenticationService.getREFRESH_TOKEN_EXPIRED_TIME_MS());
        cookie.setHttpOnly(true);

        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(cookie);
    }
}

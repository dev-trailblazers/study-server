package com.project.study.config;


import com.project.study.domain.member.JoinPlatform;
import com.project.study.domain.member.Member;
import com.project.study.security.jwt.JwtService;
import com.project.study.security.model.CustomUserDetails;
import com.project.study.security.model.oauth2.KakaoOAuth2Response;
import com.project.study.security.model.oauth2.OAuth2Response;
import com.project.study.service.MemberService;
import com.project.study.service.OAuth2Service;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtService jwtService;
    private final MemberService memberService;
    private final OAuth2Service oAuth2Service;


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
                        .requestMatchers("/", "/auth/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2   // login request url = {baseUrl}/oauth2/authorization/{provider}
                        .userInfoEndpoint(user -> user.userService(
                                customOAuth2UserService(passwordEncoder))
                        ).successHandler(customOAuth2LoginSuccessHandler())
                        .failureHandler(customAuthenticationFailureHandler())
                )
                .addFilterBefore(new TokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(
                        new CustomAuthenticationFilter(authenticationManager(authenticationConfiguration)),
                        UsernamePasswordAuthenticationFilter.class
                )
                .logout(logout -> logout
                        .addLogoutHandler(customLogoutHandler())
                        .deleteCookies("refreshToken")
                )
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) -> response
                                .sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage())
                        )
                )
                .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


    @Bean
    public CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler() {
        return new CustomOAuth2LoginSuccessHandler();
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public CustomLogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler();
    }


    @Bean
    public UserDetailsService customUserDetailsService() {
        return username -> memberService.findAvailableMemberByUsername(username)
                .map(member -> new CustomUserDetails(member))
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다."));
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService(PasswordEncoder passwordEncoder) {
        final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return userRequest -> {
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2Response oAuth2Response;

            //OAuth2 platform이 추가되면 해당 OAuth2Response를 구현한 후 조건문을 추가하면 된다.
            switch (registrationId) {
                case "kakao":
                    oAuth2Response = KakaoOAuth2Response.from(oAuth2User.getAttributes());
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 OAuth2 플랫폼입니다.");
            }

            Member member = oAuth2Response.toMember();

            String dummyPassword = passwordEncoder.encode("{bcrypt}" + UUID.randomUUID());
            member.setPassword(dummyPassword);
            member.setModifiedBy(0L);   //현재 유저의 아이디가 없는 상태이므로 0으로 설정

            //로그아웃 시 OAuth2에서도 로그아웃 할 것이기 때문에 OAuth2AccessToken을 저장한다.
            OAuth2AccessToken accessToken = userRequest.getAccessToken();
            jwtService.saveOAuth2Token(member.getUsername(), accessToken);

            /* customOAuth2UserService는 유저 정보를 조회하는 로직이지만, oauth2의 경우 여기서 이미 인증이 된 것이기 때문에
             * 유저가 DB에 없는 경우 자동 회원가입을 진행한다.
             */
            return memberService.findAvailableMemberByUsername(member.getUsername())
                    .map(CustomUserDetails::new)
                    .orElseGet(() -> new CustomUserDetails(memberService.save(member)));
        };
    }


    public class TokenAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            String authorization = request.getHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                log.warn("잘못된 토큰 헤더 : {}", authorization);
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorization.split(" ")[1];
            if (!jwtService.validateAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            //토큰이 유효하면 UserDetails 객체를 만들어서 SecurityContext의 Authentication으로 넣어준다.
            UserDetails userDetails = jwtService.getUserDetailsInToken(token);
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

            response.sendRedirect("http://localhost:5173");
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
            //공통 로그아웃 로직
            SecurityContextHolder.clearContext();

            //로그아웃 필터 이전에 인증 필터에서 토큰 검증을 진행했기 때문에 여기선 생략한다.
            String authorization = request.getHeader("Authorization");
            String accessToken = authorization.split(" ")[1];

            //accessToken black & refreshToken delete
            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("refreshToken"))
                    .findFirst().orElse(null)
                    .getValue();
            jwtService.removeToken(refreshToken);
            jwtService.restrictToken(accessToken);


            //OAuth2 로그아웃 로직
            JoinPlatform joinPlatform = jwtService.getJoinPlatformInToken(accessToken);
            if (joinPlatform == null || joinPlatform == JoinPlatform.BASIC) {
                return;
            }
            Long memberId = jwtService.getUserDetailsInToken(accessToken).getMember().getId();
            String username = memberService.findById(memberId).getUsername();

            String oauth2AccessToken = jwtService.getOauth2AccessToken(username);
            oAuth2Service.logout(oauth2AccessToken, joinPlatform);
        }

    }


    /**
     * 토큰 발행
     */
    private void issueTokens(HttpServletResponse response, Member member) {
        String accessToken = jwtService.issueAccessToken(member);
        String refreshToken = jwtService.issueRefreshToken(member.getId());

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtService.getREFRESH_TOKEN_EXPIRED_TIME_MS());
        cookie.setHttpOnly(true);

        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(cookie);
    }
}

package com.stoury.config.security;

import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   HandlerMappingIntrospector introspector,
                                                   MemberService memberService) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(formLogin -> formLogin
                        .usernameParameter("email")
                        .successHandler(authenticationSuccessHandler(memberService))
                        .failureHandler(authenticationFailureHandler()))
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler(memberService)))
                .authorizeHttpRequests(httpRequest -> httpRequest
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern("/login")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.POST, "/members")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/feeds/member/**")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/feeds/tag/**")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/feeds/popular/*")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/comments/**")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/rank/**")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/diaries/**")).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exHandler -> exHandler
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendError(401, "Not authorized.")))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration autConfig) throws Exception {
        return autConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(MemberService memberService) {
        return (request, response, authentication) -> {
            Long memberId = ((AuthenticatedMember) authentication.getPrincipal()).getId();
            Double latitude = Optional.ofNullable(request.getParameter("latitude"))
                    .map(Double::parseDouble).orElse(null);
            Double longitude = Optional.ofNullable(request.getParameter("longitude"))
                    .map(Double::parseDouble).orElse(null);
            memberService.setOnline(memberId, latitude, longitude);
            response.setStatus(200);
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(MemberService memberService) {
        return (request, response, authentication) -> {
            Long memberId = ((AuthenticatedMember) authentication.getPrincipal()).getId();
            memberService.setOffline(memberId);
            response.setStatus(200);
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, authentication) -> response.sendError(403);
    }
}

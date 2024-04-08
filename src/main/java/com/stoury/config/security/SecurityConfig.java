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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   MemberService memberService) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOriginPatterns(List.of(
                            "http://localhost:*",
                            "https://localhost:*",
                            "http://127.0.0.1:*",
                            "https://127.0.0.1:*"));
                    corsConfiguration.setAllowedMethods(List.of("GET","POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowCredentials(true);
                    corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
                    return corsConfiguration;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .successHandler(authenticationSuccessHandler(memberService))
                        .failureHandler(authenticationFailureHandler()))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler(memberService)))
                .authorizeHttpRequests(httpRequest -> httpRequest
                        .requestMatchers(requestMatcher("/login")).permitAll()
                        .requestMatchers(requestMatcher("/members", POST)).permitAll()
                        .requestMatchers(requestMatcher("/")).permitAll()
                        .requestMatchers(requestMatcher("/feeds/*")).permitAll()
                        .requestMatchers(requestMatcher("/feeds/member/**", GET)).permitAll()
                        .requestMatchers(requestMatcher("/feeds/tag/**", GET)).permitAll()
                        .requestMatchers(requestMatcher("/feeds/popular/*", GET)).permitAll()
                        .requestMatchers(requestMatcher("/comments/**", GET)).permitAll()
                        .requestMatchers(requestMatcher("/rank/**", GET)).permitAll()
                        .requestMatchers(requestMatcher("/diaries/**", GET)).permitAll()
                        .requestMatchers(requestMatcher("/actuator/**", GET)).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exHandler -> exHandler
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendError(UNAUTHORIZED.value(), "Not authorized.")))
                .build();
    }

    public AntPathRequestMatcher requestMatcher(String path, HttpMethod method) {
        return new AntPathRequestMatcher(path, method.name());
    }

    public AntPathRequestMatcher requestMatcher(String path) {
        return new AntPathRequestMatcher(path);
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
            response.setStatus(OK.value());
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(MemberService memberService) {
        return (request, response, authentication) -> {
            Long memberId = ((AuthenticatedMember) authentication.getPrincipal()).getId();
            memberService.setOffline(memberId);
            response.setStatus(OK.value());
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, authentication) -> response.sendError(FORBIDDEN.value());
    }
}

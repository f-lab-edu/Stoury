package com.stoury.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoury.dto.LoginRequest;
import com.stoury.exception.LoginRequestBindingException;
import com.stoury.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.ArrayList;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Value("${token-secret}")
    private String tokenSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(httpRequest -> httpRequest
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern("/login")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.POST, "/members")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/feeds/member/**")).permitAll()
                        .requestMatchers(new MvcRequestMatcher.Builder(introspector).pattern(HttpMethod.GET, "/feeds/tag/**")).permitAll()
                        .anyRequest().authenticated())
                .addFilter(authenticationFilter())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration autConfig) throws Exception {
        return autConfig.getAuthenticationManager();
    }

    @Bean
    public UsernamePasswordAuthenticationFilter authenticationFilter() {
        return new UsernamePasswordAuthenticationFilter() {
            @Override
            @Autowired
            public void setAuthenticationManager(AuthenticationManager authenticationManager) {
                super.setAuthenticationManager(authenticationManager);
            }

            @Override
            public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
                LoginRequest loginRequest;
                try {
                    loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
                } catch (Exception e) {
                    throw new LoginRequestBindingException(e);
                }

                return getAuthenticationManager().authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.email(),
                                loginRequest.password(),
                                new ArrayList<>())
                );
            }

            @Override
            protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
                UserDetails loginMember = (UserDetails) authResult.getPrincipal();
                String email = Objects.requireNonNull(loginMember.getUsername(), "Login email cannot be null.");

                String token = JwtUtils.issueToken(email, tokenSecret);
                response.addHeader("Authorization", token);
            }
        };
    }
}

package com.stoury.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoury.dto.LoginMemberDetails;
import com.stoury.dto.LoginRequest;
import com.stoury.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    @Value("${token-secret}")
    private String TOKEN_SECRET;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(httpRequest -> httpRequest.requestMatchers("/login", "/members").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/members", "post")).permitAll()
                        .anyRequest().authenticated())
                .addFilter(authenticationFilter())
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
                    throw new RuntimeException(e);
                }

                return getAuthenticationManager().authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.email(),
                                loginRequest.password(),
                                new ArrayList<>())
                );
            }

            @Override
            protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
                LoginMemberDetails loginMember = (LoginMemberDetails) authResult.getPrincipal();
                String email = Objects.requireNonNull(loginMember.getEmail(), "Login email cannot be null.");

                String token = JwtUtils.issueToken(email, TOKEN_SECRET);
                response.addHeader("Authorization", "Bearer " + token);
            }
        };
    }
}

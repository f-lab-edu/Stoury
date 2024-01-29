package com.stoury.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoury.dto.LoginMemberDetails;
import com.stoury.dto.LoginRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.service.MemberService;
import com.stoury.utils.JwtUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration autConfig) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(httpRequest -> httpRequest.requestMatchers("/login", "/members").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/members", "post")).permitAll()
                        .anyRequest().authenticated())
                .addFilter(new AuthenticationFilter(autConfig.getAuthenticationManager()))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
        public AuthenticationFilter(AuthenticationManager authenticationManager) {
            super(authenticationManager);
        }

        @Override
        public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws org.springframework.security.core.AuthenticationException {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginRequest;
            try {
                loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return getAuthenticationManager()
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password(),
                            new ArrayList<>()
                    ));
        }

        @Override
        protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
            LoginMemberDetails loginMember = (LoginMemberDetails) authResult.getPrincipal();
            String email = Objects.requireNonNull(loginMember.getEmail(), "Login email cannot be null.");

            String token = JwtUtils.issueToken(email);
            response.addHeader("Authorization", "Bearer " + token);
        }
    }
}

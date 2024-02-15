package com.stoury.config;

import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthorizationHeaderFilter implements Filter {
    private final JwtUtils jwtUtils;
    AntPathMatcher pathMather = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (pathMather.match("/chats/to/**", httpServletRequest.getServletPath())) {
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) httpServletRequest.getUserPrincipal();
            AuthenticatedMember authenticatedMember = (AuthenticatedMember) auth.getPrincipal();
            responseWrapper.setHeader("Authorization", jwtUtils.issueToken(authenticatedMember.getId()));
        }
        chain.doFilter(request, response);
    }
}

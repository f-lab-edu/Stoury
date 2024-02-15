package com.stoury.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    @Value("${token.secret}")
    private String tokenSecret;

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(tokenSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getMemberId(String token) {
        if (token.startsWith("Bearer")) {
            token = token.replace("Bearer", "");
        }
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String issueToken(Long memberId) {
        return Jwts.builder()
                .setSubject(memberId.toString())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 5))
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();
    }
}

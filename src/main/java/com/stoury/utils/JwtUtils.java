package com.stoury.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {
    public static String issueToken(String userEmail, final String TOKEN_SECRET) {
        return Jwts.builder()
                .setSubject(userEmail)
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS512, TOKEN_SECRET)
                .compact();
    }

    public static String getEmailFromToken(String token, String tokenSecret) {
        return Jwts.parser().setSigningKey(tokenSecret)
                .parseClaimsJws(token).getBody()
                .getSubject();
    }
}

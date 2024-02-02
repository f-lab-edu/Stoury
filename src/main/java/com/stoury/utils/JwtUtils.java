package com.stoury.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtils {
    public static String issueToken(String userEmail, final String TOKEN_SECRET) {
        return Jwts.builder()
                .setSubject(userEmail)
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS512, TOKEN_SECRET)
                .compact();
    }
}

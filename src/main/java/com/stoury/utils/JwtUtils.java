package com.stoury.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtils {
    // TODO 토큰 시크릿 숨기기
    private static final String TOKEN_SECRET = "TOKEN_SECRET";

    public static String issueToken(String userEmail) {
        return Jwts.builder()
                .setSubject(userEmail)
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS512, "TEMP_SECRET")
                .compact();
    }
}

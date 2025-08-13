package com.tonz.tonzdocs.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Secret cho access & refresh (nên để vào application.properties)
    private final String ACCESS_SECRET  = "tonz-access-secret-key-12345678901234567890";
    private final String REFRESH_SECRET = "tonz-refresh-secret-key-12345678901234567890";

    // Thời gian sống
    private final long ACCESS_EXPIRATION  = 1000 * 60 * 15;                 // 15 phút
    private final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;       // 7 ngày

    private final Key accessKey  = Keys.hmacShaKeyFor(ACCESS_SECRET.getBytes());
    private final Key refreshKey = Keys.hmacShaKeyFor(REFRESH_SECRET.getBytes());

    /* ================== GENERATE ================== */

    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** ✅ Alias để không phá code cũ nếu có nơi còn gọi generateToken */
    public String generateToken(String email) {
        return generateAccessToken(email);
    }

    /* ================== EXTRACT ================== */

    /** Parse ACCESS token và lấy email */
    public String extractEmail(String token) {
        return getAccessClaims(token).getSubject();
    }

    /** ✅ Alias cho JwtFilter đang gọi extractEmailFromAccess */
    public String extractEmailFromAccess(String token) {
        return extractEmail(token);
    }

    /** Parse REFRESH token và lấy email */
    public String extractEmailFromRefresh(String token) {
        return getRefreshClaims(token).getSubject();
    }

    /* ================== VALIDATE ================== */

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return username.equals(userDetails.getUsername()) && !isAccessTokenExpired(token);
    }

    public boolean validateRefreshToken(String token) {
        try {
            getRefreshClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /* ================== HELPERS ================== */

    private boolean isAccessTokenExpired(String token) {
        return getAccessClaims(token).getExpiration().before(new Date());
    }

    private Claims getAccessClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims getRefreshClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

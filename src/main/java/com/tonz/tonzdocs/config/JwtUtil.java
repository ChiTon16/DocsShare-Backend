package com.tonz.tonzdocs.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "tonz-super-secret-key-tonz-super-secret-key"; // ít nhất 32 ký tự
    private final long EXPIRATION_TIME = 86400000; // 1 ngày (ms)

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Tạo token từ email
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Trích xuất email từ token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Kiểm tra token có hợp lệ không
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Check hết hạn
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Parse token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

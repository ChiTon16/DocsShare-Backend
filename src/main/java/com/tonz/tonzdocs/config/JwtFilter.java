package com.tonz.tonzdocs.config;

import com.tonz.tonzdocs.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String path   = request.getServletPath();
        final String method = request.getMethod();

        // Bỏ qua preflight và các endpoint auth (login/refresh/register)
        if ("OPTIONS".equalsIgnoreCase(method) || isPublic(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                String email = jwtUtil.extractEmailFromAccess(token); // chỉ parse ACCESS
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (jwtUtil.validateAccessToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        log.warn("Invalid or expired access token");
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT filter error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublic(String path, String method) {
        // chỉ những endpoint thật sự public
        if ("POST".equalsIgnoreCase(method) &&
                ("/api/auth/login".equals(path) || "/api/auth/register".equals(path))) {
            return true;
        }
        if ("/api/auth/refresh-token".equals(path)) return true;
        // các API public khác (nếu bạn có)
        if ("GET".equalsIgnoreCase(method) &&
                ("/api/schools".equals(path) || "/api/majors".equals(path))) {
            return true;
        }
        return false; // /api/auth/me KHÔNG public
    }
}

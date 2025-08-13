package com.tonz.tonzdocs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter; // Bộ lọc đọc JWT cho nhóm /api/**

    /**
     * CORS chỉ cần cho nhóm /api/** (dev bằng Vite 5173 hoặc domain khác).
     * Admin web & admin API chạy cùng origin => không cần CORS.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(
                "http://localhost:5173"  // Vite dev (nếu bạn gọi /api/** từ FE)
                // ,"https://web.example.com" // thêm domain production nếu có
        ));
        cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Authorization","Location","Content-Disposition"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return source;
    }

    /**
     * (1) PUBLIC API: /api/**
     * - Stateless + JWT
     * - Dùng cho frontend tách rời (React/Vite, mobile...)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(c -> {}) // dùng bean corsConfigurationSource() ở trên
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Cho phép preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        // Các endpoint mở
                        .requestMatchers("/api/auth/**", "/api/documents", "/api/subjects").permitAll()

                        // Còn lại cần JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * (2) WEB + ADMIN (SSR): mọi thứ còn lại, bao gồm:
     * - /admin/** (trang web)
     * - /admin/api/** (AJAX từ trang admin)  -> DÙNG SESSION, KHÔNG DÙNG JWT
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public assets/pages
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico",
                                "/login", "/login?logout", "/error").permitAll()

                        // Admin AJAX dùng session
                        .requestMatchers(HttpMethod.OPTIONS, "/admin/api/**").permitAll()
                        .requestMatchers("/admin/api/**").hasRole("ADMIN")

                        // Trang admin SSR
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Mọi thứ khác yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // AJAX admin không cần CSRF cho gọn (hoặc bạn tự gửi X-CSRF-TOKEN nếu muốn bật)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/admin/api/**"))
                // Session cho web
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }
}

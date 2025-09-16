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
    private JwtFilter jwtFilter; // chỉ áp cho /api/**

    /* ------------ CORS (cho dev FE http://localhost:5173) ------------ */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:5173")); // FE dev
        cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept"));
        cors.setExposedHeaders(List.of("Authorization","Location","Content-Disposition"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // REST (JWT)
        source.registerCorsConfiguration("/api/**", cors);
        // SockJS handshake, /ws/info, /ws/** long-polling
        source.registerCorsConfiguration("/api/ws/**", cors);
        return source;
    }

    /* ------------ (1) PUBLIC API /api/** (stateless + JWT) ------------ */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(c -> {}) // dùng bean ở trên
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        // Các endpoint mở (SỬA CÚ PHÁP BỊ LỖI)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/subjects/**",
                                "/api/schools/**",
                                "/api/majors/**",
                                "/api/documents/**",
                                "/api/comments/**"
                        ).permitAll()

                        // ✅ Cho phép bắt tay SockJS
                        .requestMatchers("/api/ws/**").permitAll()

                        // Chat REST cần JWT:
                        .requestMatchers("/api/chat/**").authenticated()

                        // Còn lại cần JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /* ------------ (2) WEB/ADMIN cho phần còn lại ------------ */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico",
                                "/login", "/login?logout", "/error").permitAll()

                        // Cho phép preflight & handshake SockJS
                        .requestMatchers(HttpMethod.OPTIONS, "/api/ws/**").permitAll()
                        .requestMatchers("api/ws/**").permitAll()

                        // Admin AJAX dùng session
                        .requestMatchers(HttpMethod.OPTIONS, "/admin/api/**").permitAll()
                        .requestMatchers("/admin/api/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

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
                .csrf(csrf -> csrf.ignoringRequestMatchers("/admin/api/**"))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }
}

package com.tonz.tonzdocs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter; // Bộ lọc để đọc JWT từ header

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/documents/**").authenticated() // yêu cầu JWT token
                        .requestMatchers("/", "/hello", "/login", "/api/auth/**", "/api/documents").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN") // chỉ ROLE_ADMIN mới được vào
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // không dùng session
                );
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .loginProcessingUrl("/login")
//                        .defaultSuccessUrl("/admin/dashboard", true)
//                        .failureUrl("/login?error=true")
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout")
//                        .permitAll()
//                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // Thêm bộ lọc JWT

        return http.build();
    }
}

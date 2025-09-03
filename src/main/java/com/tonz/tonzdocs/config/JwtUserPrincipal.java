// src/main/java/com/tonz/tonzdocs/config/JwtUserPrincipal.java
package com.tonz.tonzdocs.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class JwtUserPrincipal implements UserDetails {
    private final Integer userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserPrincipal(Integer userId, String email, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.authorities = authorities;
    }

    public Integer getUserId() { return userId; }
    public String getEmail() { return email; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; }     // không dùng
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

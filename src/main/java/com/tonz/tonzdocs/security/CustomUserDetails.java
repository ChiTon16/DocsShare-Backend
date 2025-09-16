// src/main/java/com/tonz/tonzdocs/security/CustomUserDetails.java
package com.tonz.tonzdocs.security;

import com.tonz.tonzdocs.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Integer userId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public CustomUserDetails(Integer userId,
                             String email,
                             String password,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean enabled) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.authorities = (authorities != null ? authorities : List.of());
        this.enabled = enabled;
    }

    /** Tạo từ entity User của bạn */
    public static CustomUserDetails from(User u) {
        // Nếu chưa có bảng role, cho mặc định ROLE_USER
        List<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new CustomUserDetails(
                u.getUserId(),
                u.getEmail(),
                u.getPassword() == null ? "" : u.getPassword(),
                auth,
                true
        );
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}

package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        String roleName = user.getRole().getName(); // ví dụ: "admin", "guest"

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + roleName.toUpperCase()) // thêm ROLE_
                .build();
    }

}

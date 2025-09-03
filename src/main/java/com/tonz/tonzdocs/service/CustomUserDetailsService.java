package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + email));
        return new CustomUserDetails(user); // <-- trả về CustomUserDetails
    }
}
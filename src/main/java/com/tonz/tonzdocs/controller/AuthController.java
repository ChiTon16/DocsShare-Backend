package com.tonz.tonzdocs.controller;

import com.tonz.tonzdocs.dto.RegisterRequest;
import com.tonz.tonzdocs.dto.LoginRequest;
import com.tonz.tonzdocs.model.Role;
import com.tonz.tonzdocs.model.School;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.RoleRepository;
import com.tonz.tonzdocs.repository.SchoolRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private SchoolRepository schoolRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      BindingResult result) {
        if (result.hasErrors()) {
            // Trả về danh sách lỗi chi tiết
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err ->
                    errors.put(err.getField(), err.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // Xử lý đăng ký user...
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        return authService.login(request);
    }


}

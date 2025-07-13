package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.config.JwtUtil;
import com.tonz.tonzdocs.dto.LoginRequest;
import com.tonz.tonzdocs.dto.RegisterRequest;
import com.tonz.tonzdocs.model.Role;
import com.tonz.tonzdocs.model.School;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.MajorRepository;
import com.tonz.tonzdocs.repository.RoleRepository;
import com.tonz.tonzdocs.repository.SchoolRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private SchoolRepository schoolRepo;

    @Autowired
    private MajorRepository majorRepo;



    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<?> login(LoginRequest request) {
        Optional<User> userOpt = userRepo.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Tài khoản không tồn tại"));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Mật khẩu không đúng"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Tài khoản đã bị khóa"));
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    public ResponseEntity<?> register(RegisterRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", "Email đã được sử dụng"));
        }

        // 2. Tạo user mới
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true); // ✅ mặc định là active

        // 3. Gán role
        Role role = roleRepo.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + request.getRoleId()));
        user.setRole(role);

        // 4. Gán trường (nếu có)
        if (request.getSchoolId() != null) {
            School school = schoolRepo.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy trường học với ID: " + request.getSchoolId()));
            user.setSchool(school);
        }

        // 5. Gán ngành học (nếu có)
        if (request.getMajorId() != null) {
            // ⚠️ Cần thêm MajorRepository nếu chưa có
            majorRepo.findById(request.getMajorId()).ifPresent(user::setMajor);
        }

        // 6. Lưu user vào DB
        userRepo.save(user);

        return ResponseEntity.ok(Collections.singletonMap("message", "Đăng ký thành công!"));
    }

}

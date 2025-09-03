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
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepo;
    @Autowired private SchoolRepository schoolRepo;
    @Autowired private MajorRepository majorRepo;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UploadService uploadService; // 👈 dùng service có sẵn của bạn

    /* =========================================================
     * LOGIN: trả về { token, refreshToken } + (tùy chọn) set cookie refreshToken (HttpOnly)
     * Frontend hiện tại của bạn mong nhận { token, refreshToken } và sẽ tự set accessToken vào cookie/js-cookie
     * ========================================================= */
    public ResponseEntity<?> login(LoginRequest request) {
        Optional<User> userOpt = userRepo.findByEmail(request.getEmail());

        // Không tìm thấy email
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "ok", false,
                    "code", "USER_NOT_FOUND",
                    "message", "Tài khoản không tồn tại"
            ));
        }

        User user = userOpt.get();

        // Sai mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(Map.of(
                    "ok", false,
                    "code", "INVALID_PASSWORD",
                    "message", "Mật khẩu không đúng"
            ));
        }

        // Tài khoản bị khóa
        if (!user.isActive()) {
            return ResponseEntity.ok(Map.of(
                    "ok", false,
                    "code", "ACCOUNT_LOCKED",
                    "message", "Tài khoản đã bị khóa"
            ));
        }

        // Đăng nhập thành công
        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "token", token,
                "refreshToken", refreshToken
        ));
    }


    /* =========================================================
     * REFRESH TOKEN: đọc refresh từ cookie (ưu tiên) hoặc Authorization: Bearer <refresh>
     * Trả về { token, refreshToken } (có rotate refresh) + set lại cookie
     * ========================================================= */
    public ResponseEntity<?> refreshToken(String refreshCookie, String authHeader) {
        // Lấy refresh từ cookie, nếu không có thì thử từ Bearer
        String refresh = refreshCookie;
        if ((refresh == null || refresh.isBlank()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            refresh = authHeader.substring(7);
        }
        if (refresh == null || refresh.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Missing refresh token"));
        }

        try {
            // Lấy email từ refresh & kiểm tra user còn active
            String email = jwtUtil.extractEmailFromRefresh(refresh);
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Tài khoản đã bị khóa"));
            }

            // Cấp token mới (rotate refresh để an toàn)
            String newAccess  = jwtUtil.generateAccessToken(user.getEmail());
            String newRefresh = jwtUtil.generateRefreshToken(user.getEmail());

            ResponseCookie newRefreshCookie = ResponseCookie.from("refreshToken", newRefresh)
                    .httpOnly(true)
                    .secure(false)        // ⚠️ production HTTPS -> true
                    .sameSite("Lax")      // ⚠️ khác domain -> "None" + secure(true)
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                    .body(Map.of(
                            "token", newAccess,
                            "refreshToken", newRefresh
                    ));
        } catch (JwtException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid refresh token"));
        }
    }

    /* =========================================================
     * REGISTER giữ nguyên (chỉ tidy nhẹ)
     * ========================================================= */
    public ResponseEntity<?> register(RegisterRequest request, MultipartFile avatar) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", "Email đã được sử dụng"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        Role role = roleRepo.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + request.getRoleId()));
        user.setRole(role);

        if (request.getSchoolId() != null) {
            School school = schoolRepo.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy trường học với ID: " + request.getSchoolId()));
            user.setSchool(school);
        }
        if (request.getMajorId() != null) {
            majorRepo.findById(request.getMajorId()).ifPresent(user::setMajor);
        }

        // Lưu lần 1 để có userId
        userRepo.save(user);

        // Nếu có file avatar -> upload Cloudinary & update URL
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String publicId = "user_" + user.getUserId(); // dễ overwrite khi update
                String secureUrl = uploadService.uploadAvatar(avatar, publicId);
                user.setAvatarUrl(secureUrl);
                userRepo.save(user); // update URL
            } catch (IOException ex) {
                // Không fail đăng ký vì upload lỗi; có thể log warning nếu muốn
                // log.warn("Upload avatar failed", ex);
            }
        } else if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            // Trường hợp FE đã có sẵn link (ít dùng khi đã upload qua server)
            user.setAvatarUrl(request.getAvatarUrl().trim());
            userRepo.save(user);
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "Đăng ký thành công!"));
    }

    // 👇 NEW: helper tạo Gravatar URL (identicon) theo email
    private String buildGravatarUrl(String email) {
        try {
            String normalized = (email == null ? "" : email.trim().toLowerCase());
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            String hash = sb.toString();
            // d=identicon: avatar tự sinh; s=160: kích thước
            return "https://www.gravatar.com/avatar/" + hash + "?d=identicon&s=160";
        } catch (Exception e) {
            // fallback: generic avatar
            return "https://www.gravatar.com/avatar/?d=mp&s=160";
        }
    }
}

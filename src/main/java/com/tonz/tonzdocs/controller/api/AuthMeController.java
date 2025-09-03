package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.UserDTO;
import com.tonz.tonzdocs.model.Major;
import com.tonz.tonzdocs.model.School;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthMeController {

    private final UserRepository userRepo; // JpaRepository<User, Integer>

    @GetMapping("/me")
    public UserDTO me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

        // Thường là email; đôi khi bạn set principal là userId dạng string
        String nameOrId = auth.getName();

        Optional<User> userOpt = userRepo.findByEmail(nameOrId);
        if (userOpt.isEmpty() && nameOrId.matches("\\d+")) {
            // ✅ Entity id là Integer -> dùng Integer.parseInt
            userOpt = userRepo.findById(Integer.parseInt(nameOrId));
        }
        User u = userOpt.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Lấy role: ưu tiên từ DB (String), fallback từ authorities
        String roleFromAuth = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse(null);
        String roleName = u.getRole() != null ? u.getRole().getName() : roleFromAuth;

        // Nếu User liên kết tới entity School/Major
        Integer schoolId = null;
        Integer majorId  = null;
        School sc = u.getSchool();
        if (sc != null) {
            // ✅ School dùng getSchoolId()
            schoolId = sc.getSchoolId();
        }
        Major mj = u.getMajor();
        if (mj != null) {
            // ✅ Major dùng getMajorId()
            majorId = mj.getId();
        }

        // Nếu User không có quan hệ mà chỉ có field schoolId/majorId trên User,
        // bạn có thể thay 2 block trên bằng:
        // Integer schoolId = u.getSchoolId();
        // Integer majorId  = u.getMajorId();

        UserDTO dto = new UserDTO(
                u.getUserId(),          // ✅ dùng getUserId()
                u.getName(),
                u.getEmail(),
                null,                   // không trả password
                schoolId,
                majorId,
                roleName,
                u.isActive()
        );
        dto.setAvatarUrl(u.getAvatarUrl()); // 🔹 set avatar vào DTO
        return dto;
    }
}

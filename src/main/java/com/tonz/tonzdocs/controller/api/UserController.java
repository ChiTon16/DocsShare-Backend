// src/main/java/com/tonz/tonzdocs/controller/api/UserController.java
package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.UserDTO;
import com.tonz.tonzdocs.model.Major;
import com.tonz.tonzdocs.model.School;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Integer id) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Lấy role name (ưu tiên từ DB nếu có)
        String roleName = u.getRole() != null ? u.getRole().getName() : null;

        // Lấy schoolId & majorId (từ quan hệ hoặc field thô nếu bạn lưu trực tiếp trên User)
        Integer schoolId = null;
        Integer majorId  = null;

        School sc = u.getSchool();
        if (sc != null) {
            schoolId = sc.getSchoolId(); // chú ý getter theo entity của bạn
        }

        Major mj = u.getMajor();
        if (mj != null) {
            majorId = mj.getId(); // chú ý getter theo entity của bạn
        }

        // Nếu bạn không map quan hệ mà lưu trực tiếp id trên User, thay bằng:
        // schoolId = u.getSchoolId();
        // majorId  = u.getMajorId();

        UserDTO dto = new UserDTO(
                u.getUserId(),     // id
                u.getName(),
                u.getEmail(),
                null,              // không trả password
                schoolId,
                majorId,
                roleName,
                u.isActive()
        );
        dto.setAvatarUrl(u.getAvatarUrl());
        return dto;
    }
}

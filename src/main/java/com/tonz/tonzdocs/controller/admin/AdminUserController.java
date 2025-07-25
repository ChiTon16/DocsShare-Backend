package com.tonz.tonzdocs.controller.admin;

import com.tonz.tonzdocs.dto.UserDTO;
import com.tonz.tonzdocs.model.Major;
import com.tonz.tonzdocs.model.Role;
import com.tonz.tonzdocs.model.School;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.MajorRepository;
import com.tonz.tonzdocs.repository.RoleRepository;
import com.tonz.tonzdocs.repository.SchoolRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder; // Thêm để mã hóa mật khẩu

    @Autowired
    private SchoolRepository schoolRepo;

    @Autowired
    private MajorRepository majorRepo;

    // GET /admin/users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepo.findAll();
        List<UserDTO> result = users.stream()
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole() != null ? user.getRole().getName() : null,
                        user.isActive()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }


    // PUT /admin/users/{id}/ban
    @PutMapping("/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable Integer id) {
        return userRepo.findById(id).map(user -> {
            user.setActive(false); // hoặc user.setBanned(true);
            userRepo.save(user);
            return ResponseEntity.ok().body("Tài khoản đã bị chặn.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // POST /admin/api/users
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        if (userRepo.findByEmail(userDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email đã tồn tại.");
        }

        // Tìm hoặc tạo vai trò
        Role role = roleRepo.findByName(userDTO.getRole())
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(userDTO.getRole());
                    return roleRepo.save(newRole);
                });

        // Tìm School dựa trên schoolId
        School school = schoolRepo.findById(userDTO.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found with id: " + userDTO.getSchoolId()));

        // Tìm Major dựa trên majorId
        Major major = majorRepo.findById(userDTO.getMajorId())
                .orElseThrow(() -> new RuntimeException("Major not found with id: " + userDTO.getMajorId()));

        // Tạo mới người dùng
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setSchool(school); // Gán đối tượng School
        user.setMajor(major);   // Gán đối tượng Major
        user.setRole(role);
        user.setActive(userDTO.isActive());

        User savedUser = userRepo.save(user);

        // Chuẩn bị response DTO (không bao gồm password)
        UserDTO responseDTO = new UserDTO(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                null, // Không trả về mật khẩu
                savedUser.getSchool() != null ? savedUser.getSchool().getId() : null, // Trả về schoolId
                savedUser.getMajor() != null ? savedUser.getMajor().getId() : null,   // Trả về majorId
                savedUser.getRole() != null ? savedUser.getRole().getName() : null,
                savedUser.isActive()
        );

        return ResponseEntity.ok(responseDTO);
    }
}

package com.tonz.tonzdocs.controller.admin;

import com.tonz.tonzdocs.dto.UserDTO;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepo;

    // GET /admin/users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepo.findAll();
        List<UserDTO> result = users.stream()
                .map(user -> new UserDTO(
                        user.getUserId(), // hoặc user.getId() tùy bạn đặt tên
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
}

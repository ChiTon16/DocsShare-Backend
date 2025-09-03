// src/main/java/com/tonz/tonzdocs/controller/api/RecentSubjectController.java
package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.RecentSubjectDTO;
import com.tonz.tonzdocs.security.CustomUserDetails;
import com.tonz.tonzdocs.service.RecentSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")                // tùy cấu trúc router của bạn
@RequiredArgsConstructor
public class RecentSubjectController {

    private final RecentSubjectService recentSubjectService;   // <-- TIÊM SERVICE

    @GetMapping("/recent-subjects")
    public ResponseEntity<List<RecentSubjectDTO>> list(
            @AuthenticationPrincipal CustomUserDetails user,   // đã lấy từ JWT
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(
                recentSubjectService.getRecentSubjects(user.getUserId(), size)
        );
    }
}

// src/main/java/com/tonz/tonzdocs/controller/api/StudyPlanController.java
package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.config.JwtUserPrincipal;
import com.tonz.tonzdocs.dto.StudyPlanDTO;
import com.tonz.tonzdocs.dto.StudyPlanItemDTO;
import com.tonz.tonzdocs.security.CustomUserDetails;
import com.tonz.tonzdocs.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService service;

    // -------- helpers --------
    private Integer extractUserId(Object principal) {
        if (principal instanceof JwtUserPrincipal p) {
            return p.getUserId();
        }
        if (principal instanceof CustomUserDetails c) {
            return c.getUserId();
        }
        return null;
    }

    // -------- endpoints --------

    @GetMapping
    public ResponseEntity<List<StudyPlanDTO>> myPlans(
            @AuthenticationPrincipal Object principal
            // Nếu FE có truyền ?docId=... để đánh dấu contains, lúc sau có thể thêm vào service
            // @RequestParam(name = "docId", required = false) Integer docId
    ) {
        Integer userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(401).build();

        // Nếu sau này có phương thức service.listPlans(userId, docId) thì dùng ở đây.
        List<StudyPlanDTO> plans = service.listPlans(userId);
        return ResponseEntity.ok(plans);
    }

    @PostMapping
    public ResponseEntity<StudyPlanDTO> create(
            @AuthenticationPrincipal Object principal,
            @RequestParam String name,
            @RequestParam(required = false) String description
    ) {
        Integer userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(401).build();

        StudyPlanDTO dto = service.createPlan(userId, name, description);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{planId}/items")
    public ResponseEntity<List<StudyPlanItemDTO>> items(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer planId
    ) {
        Integer userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(401).build();

        List<StudyPlanItemDTO> items = service.listItems(userId, planId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{planId}/items/{documentId}")
    public ResponseEntity<Void> addItem(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer planId,
            @PathVariable Integer documentId,
            @RequestParam(required = false) String note
    ) {
        Integer userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(401).build();

        service.addItem(userId, planId, documentId, note);
        return ResponseEntity.noContent().build(); // 204
    }

    @DeleteMapping("/{planId}/items/{documentId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer planId,
            @PathVariable Integer documentId
    ) {
        Integer userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(401).build();

        service.removeItem(userId, planId, documentId);
        return ResponseEntity.noContent().build(); // 204
    }
}

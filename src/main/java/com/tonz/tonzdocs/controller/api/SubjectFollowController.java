// SubjectFollowController.java
package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubjectFollowController {

    private final FollowService followService;

    // Danh sách course user đang theo dõi
    @GetMapping("/users/{userId}/subjects")
    public ResponseEntity<List<SubjectDTO>> list(@PathVariable Integer userId) {
        return ResponseEntity.ok(followService.listFollowed(userId));
    }

    // Follow
    @PostMapping("/subjects/{subjectId}/follow")
    public ResponseEntity<?> follow(@RequestParam Integer userId, @PathVariable Integer subjectId) {
        boolean created = followService.follow(userId, subjectId);
        return ResponseEntity.ok().body(new FollowResp(true, created ? "followed" : "already"));
    }

    // Unfollow
    @DeleteMapping("/subjects/{subjectId}/follow")
    public ResponseEntity<?> unfollow(@RequestParam Integer userId, @PathVariable Integer subjectId) {
        boolean removed = followService.unfollow(userId, subjectId);
        return ResponseEntity.ok().body(new FollowResp(false, removed ? "unfollowed" : "not-following"));
    }

    record FollowResp(boolean following, String msg) {}

    @GetMapping("/subjects/{subjectId}/followers/count")
    public ResponseEntity<Map<String, Long>> getFollowersCount(@PathVariable Integer subjectId) {
        long count = followService.countFollowers(subjectId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}

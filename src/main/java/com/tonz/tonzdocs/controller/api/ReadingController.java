package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.ContinueCardDTO;
import com.tonz.tonzdocs.dto.DocumentViewDTO;
import com.tonz.tonzdocs.dto.ProgressUpsertRequest;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.security.CustomUserDetails;
import com.tonz.tonzdocs.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;
    private final DocumentRepository documentRepo;
    private final UserRepository userRepo;

    /** Upsert tiến độ đọc – đúng URL bạn đang gọi trong Postman */
    @PostMapping("/recent-views/upsert")
    public ResponseEntity<?> upsertProgress(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody ProgressUpsertRequest req
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (req.documentId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "document_id is required"));
        }

        User user = userRepo.findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Document doc = documentRepo.findById(req.documentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        readingService.upsertProgress(user, doc, req);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /** Danh sách Continue Reading (percent < 100, sort by viewedAt desc) */
    @GetMapping("/reading/continue")
    public Page<ContinueCardDTO> getContinue(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepo.findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return readingService.getContinueCards(user, pageable);
    }

    /** Danh sách Recently Viewed (mọi percent, sort by viewedAt desc) */
    @GetMapping("/reading/recent")
    public Page<ContinueCardDTO> getRecentCards(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepo.findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return readingService.getRecentCards(user, pageable);
    }

    /** (Tuỳ chọn) trả về bản đầy đủ DocumentViewDTO nếu UI cần nhiều field hơn */
    @GetMapping("/reading/recent-full")
    public Page<DocumentViewDTO> getRecentFull(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepo.findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return readingService.getRecent(user, pageable);
    }
}

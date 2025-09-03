package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.config.JwtUtil;
import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.RecentViewRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepo;
    private final UserRepository userRepo;
    private final RecentViewRepository recentViewRepo;
    private final JwtUtil jwtUtil;
    private final DocumentService documentService;

    // ---- Thumbnail: trả THẲNG ảnh PNG (không 302) ----
    @GetMapping(value = "/{documentId}/thumbnail", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "400") int width
    ) {
        byte[] png = documentService.getThumbnail(documentId, page, width);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(png);
    }

    // ---- Các API khác giữ nguyên ----
    @GetMapping
    public List<DocumentDTO> getAllDocuments() {
        List<Document> documents = documentRepo.findAll();
        return documents.stream()
                .map(DocumentService::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Integer id,
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Thiếu token"));
            }
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            var userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token không hợp lệ"));
            }
            var user = userOpt.get();

            return documentRepo.findById(id).map(doc -> {
                boolean alreadyViewed = recentViewRepo.existsByUserAndDocument(user, doc);
                if (!alreadyViewed) {
                    RecentView view = new RecentView();
                    view.setUser(user);
                    view.setDocument(doc);
                    view.setViewedAt(java.time.LocalDateTime.now());
                    recentViewRepo.save(view);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("id", doc.getDocumentId());
                response.put("title", doc.getTitle());
                response.put("fileUrl", doc.getFilePath());
                response.put("uploadedAt", doc.getUploadTime());
                response.put("uploadedBy", doc.getUser().getName());
                response.put("subject", doc.getSubject().getName());

                return ResponseEntity.ok(response);
            }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy tài liệu")));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token không hợp lệ hoặc hết hạn", "details", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(@RequestParam("q") String keyword) {
        List<Document> results = documentRepo.findByTitleContainingIgnoreCase(keyword);
        List<DocumentDTO> dtos = results.stream().map(DocumentService::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

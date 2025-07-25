package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.config.JwtUtil;
import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.RecentViewRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.service.DocumentService;
import com.tonz.tonzdocs.service.RecentViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RecentViewRepository recentViewRepo;

    @Autowired
    private JwtUtil jwtUtil;


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

        List<DocumentDTO> dtos = results.stream()
                .map(DocumentService::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }







}


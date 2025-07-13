package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserDocumentController {

    @Autowired
    private DocumentRepository documentRepo;

    @GetMapping("/{userId}/documents")
    public ResponseEntity<?> getDocumentsByUser(@PathVariable Integer userId) {
        List<Document> documents = documentRepo.findByUserUserId(userId);

        if (documents.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content nếu không có tài liệu
        }

        // ✅ Convert sang DTO để tránh rò rỉ thông tin
        List<DocumentDTO> dtos = documents.stream()
                .map(DocumentService::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}


// src/main/java/com/tonz/tonzdocs/controller/api/SubjectController.java
package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.service.DocumentService;
import com.tonz.tonzdocs.service.SubjectService;
import com.tonz.tonzdocs.service.TrendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;
    private final DocumentRepository documentRepo;

    @Autowired
    private TrendingService trendingService;

    @GetMapping
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAll());
    }

    @GetMapping("/{subjectId}/documents")
    public ResponseEntity<List<DocumentDTO>> getDocumentsBySubject(@PathVariable Integer subjectId) {
        List<DocumentDTO> documents = documentRepo
                .findBySubject_SubjectId(subjectId).stream()
                .map(DocumentService::toDTO)
                .toList();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{subjectId}/trending")
    public ResponseEntity<List<DocumentDTO>> getTrendingBySubject(
            @PathVariable Integer subjectId,
            @RequestParam(defaultValue = "18") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "48") int halfLifeHours
    ) {
        List<DocumentDTO> list = trendingService.getSubjectTrending(subjectId, limit, offset, halfLifeHours);
        return ResponseEntity.ok(list);
    }


}

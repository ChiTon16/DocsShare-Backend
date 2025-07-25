// SubjectController.java
package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.model.Subject;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.SubjectRepository;
import com.tonz.tonzdocs.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepo;

    @Autowired
    private DocumentRepository documentRepo;

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectRepo.findAll();
    }

    @GetMapping("/{subjectId}/documents")
    public ResponseEntity<List<DocumentDTO>> getDocumentsBySubject(@PathVariable Integer subjectId) {
        List<DocumentDTO> documents = documentRepo
                .findBySubject_SubjectId(subjectId).stream()
                .map(DocumentService::toDTO)
                .toList();

        return ResponseEntity.ok(documents);
    }
}

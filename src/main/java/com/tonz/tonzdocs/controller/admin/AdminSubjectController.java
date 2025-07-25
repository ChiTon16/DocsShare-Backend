package com.tonz.tonzdocs.controller.admin;

import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.model.Subject;
import com.tonz.tonzdocs.repository.SubjectRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/api/subjects")
public class AdminSubjectController {

    @Autowired
    private SubjectRepository subjectRepo;

    @GetMapping
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        List<Subject> subjects = subjectRepo.findAll();
        List<SubjectDTO> result = subjects.stream()
                .map(subject -> new SubjectDTO(
                        subject.getSubjectId(),
                        subject.getName(),
                        subject.getCode()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDTO subjectDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList());
        }

        // Kiểm tra xem tên môn học đã tồn tại chưa (tùy chọn)
        if (subjectRepo.findByName(subjectDTO.getName()).isPresent()) {
            return ResponseEntity.badRequest().body("Tên môn học đã tồn tại.");
        }

        Subject subject = new Subject();
        subject.setName(subjectDTO.getName());
        subject.setCode(subjectDTO.getDescription());

        Subject savedSubject = subjectRepo.save(subject);

        SubjectDTO responseDTO = new SubjectDTO(
                savedSubject.getSubjectId(),
                savedSubject.getName(),
                savedSubject.getCode()
        );

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {
        if (subjectRepo.existsById(id)) {
            subjectRepo.deleteById(id);
            return ResponseEntity.ok().body("Môn học đã được xóa.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
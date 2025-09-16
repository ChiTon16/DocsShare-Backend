package com.tonz.tonzdocs.controller.admin;

import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.model.Subject;
import com.tonz.tonzdocs.repository.SubjectRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        List<SubjectDTO> result = subjectRepo.findAll()
                .stream()
                .map(s -> new SubjectDTO(
                        s.getSubjectId(),
                        s.getName(),
                        s.getCode()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDTO subjectDTO, BindingResult binding) {
        // Nếu bạn chưa gắn annotation @NotBlank/... trong DTO thì vẫn kiểm tra cơ bản:
        if (binding.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    binding.getFieldErrors().stream()
                            .map(e -> e.getField() + ": " + e.getDefaultMessage())
                            .collect(Collectors.toList())
            );
        }
        if (subjectDTO.getName() == null || subjectDTO.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên môn học không được để trống.");
        }

        // Kiểm tra trùng tên (giữ nguyên theo repo hiện tại của bạn)
        if (subjectRepo.findByName(subjectDTO.getName()).isPresent()) {
            return ResponseEntity.badRequest().body("Tên môn học đã tồn tại.");
        }

        Subject subject = new Subject();
        subject.setName(subjectDTO.getName());
        subject.setCode(subjectDTO.getCode()); // ✅ trước đây dùng getDescription() nên bị lỗi

        Subject saved = subjectRepo.save(subject);

        SubjectDTO responseDTO = new SubjectDTO(
                saved.getSubjectId(),
                saved.getName(),
                saved.getCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {
        if (!subjectRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        subjectRepo.deleteById(id);
        return ResponseEntity.ok("Môn học đã được xóa.");
    }
}

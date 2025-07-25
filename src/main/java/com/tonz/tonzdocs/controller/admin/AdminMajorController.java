package com.tonz.tonzdocs.controller.admin;

import com.tonz.tonzdocs.dto.MajorDTO;
import com.tonz.tonzdocs.model.Major;
import com.tonz.tonzdocs.repository.MajorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/api/majors")
public class AdminMajorController {

    @Autowired
    private MajorRepository majorRepo;

    @GetMapping
    public ResponseEntity<List<MajorDTO>> getAllMajors() {
        List<Major> majors = majorRepo.findAll();
        List<MajorDTO> result = majors.stream()
                .map(major -> new MajorDTO(
                        major.getId(),
                        major.getName(),
                        major.getCode()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createMajor(@Valid @RequestBody MajorDTO majorDTO, BindingResult result) {
        System.out.println("Received data: " + majorDTO); // Debug log
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList());
        }

        if (majorRepo.findByCode(majorDTO.getCode()).isPresent()) {
            return ResponseEntity.badRequest().body("Mã ngành đã tồn tại.");
        }

        Major major = new Major();
        major.setName(majorDTO.getName());
        major.setCode(majorDTO.getCode());

        Major savedMajor = majorRepo.save(major);
        MajorDTO responseDTO = new MajorDTO(
                savedMajor.getId(),
                savedMajor.getName(),
                savedMajor.getCode()
        );

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMajor(@PathVariable Integer id) {
        if (majorRepo.existsById(id)) {
            majorRepo.deleteById(id);
            return ResponseEntity.ok().body("Ngành đã được xóa.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
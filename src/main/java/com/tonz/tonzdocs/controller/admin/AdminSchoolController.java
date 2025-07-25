package com.tonz.tonzdocs.controller.admin;

import com.tonz.tonzdocs.dto.SchoolDTO;
import com.tonz.tonzdocs.model.School;
import com.tonz.tonzdocs.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/api/schools")
public class AdminSchoolController {

    @Autowired
    private SchoolRepository schoolRepo;

    @GetMapping
    public ResponseEntity<List<SchoolDTO>> getAllSchools() {
        List<School> schools = schoolRepo.findAll();
        List<SchoolDTO> result = schools.stream()
                .map(school -> new SchoolDTO(
                        school.getId(),
                        school.getName(),
                        school.getAddress()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createSchool(@Valid @RequestBody SchoolDTO schoolDTO, BindingResult result) {
        System.out.println("Received data: " + schoolDTO); // Debug log
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList());
        }

        if (schoolRepo.findByName(schoolDTO.getName()).isPresent()) {
            return ResponseEntity.badRequest().body("Tên trường đã tồn tại.");
        }

        School school = new School();
        school.setName(schoolDTO.getName());
        school.setAddress(schoolDTO.getAddress());

        School savedSchool = schoolRepo.save(school);
        SchoolDTO responseDTO = new SchoolDTO(
                savedSchool.getId(),
                savedSchool.getName(),
                savedSchool.getAddress()
        );

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchool(@PathVariable Integer id) {
        if (schoolRepo.existsById(id)) {
            schoolRepo.deleteById(id);
            return ResponseEntity.ok().body("Trường học đã được xóa.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
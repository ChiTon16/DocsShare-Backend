package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.MajorDTO;
import com.tonz.tonzdocs.dto.SchoolDTO;
import com.tonz.tonzdocs.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SchoolController {

    private final SchoolService svc;

    // Lấy tất cả schools (giữ nguyên)
    @GetMapping("/schools")
    public List<SchoolDTO> getSchools() {
        return svc.getSchools();
    }

    // ✅ Thêm API mới: lấy school theo id
    @GetMapping("/schools/{id}")
    public SchoolDTO getSchoolById(@PathVariable Integer id) {
        return svc.getSchoolById(id);
    }

    // Lấy tất cả majors (giữ nguyên)
    @GetMapping("/majors")
    public List<MajorDTO> getMajors() {
        return svc.getMajors();
    }
}

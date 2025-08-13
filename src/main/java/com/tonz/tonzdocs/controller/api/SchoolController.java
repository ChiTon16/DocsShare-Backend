package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.MajorDTO;
import com.tonz.tonzdocs.dto.SchoolDTO;
import com.tonz.tonzdocs.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SchoolController {

    private final SchoolService svc;

    @GetMapping("/schools")
    public List<SchoolDTO> getSchools() {
        return svc.getSchools();
    }

    @GetMapping("/majors")
    public List<MajorDTO> getMajors() {
        return svc.getMajors();
    }
}

package com.tonz.tonzdocs.service;// service/SchoolService.java

import com.tonz.tonzdocs.dto.MajorDTO;
import com.tonz.tonzdocs.dto.SchoolDTO;
import com.tonz.tonzdocs.repository.MajorRepository;
import com.tonz.tonzdocs.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepo;
    private final MajorRepository majorRepo;

    public List<SchoolDTO> getSchools() {
        return schoolRepo.findAll(Sort.by("name").ascending())
                .stream()
                .map(s -> new SchoolDTO(s.getId(), s.getName(), s.getAddress()))
                .toList();
    }

    public List<MajorDTO> getMajors() {
        return majorRepo.findAll(Sort.by("name").ascending())
                .stream()
                .map(m -> new MajorDTO(m.getId(), m.getName(), m.getCode()))
                .toList();
    }
}

// src/main/java/com/tonz/tonzdocs/service/SubjectService.java
package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {
    private final SubjectRepository subjectRepo;

    public List<SubjectDTO> getAll() {
        return subjectRepo.findAllDTO();
    }
}

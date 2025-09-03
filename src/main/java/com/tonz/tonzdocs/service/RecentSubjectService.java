// service/RecentSubjectService.java
package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.RecentSubjectDTO;
import com.tonz.tonzdocs.repository.RecentSubjectRepository;
import com.tonz.tonzdocs.repository.RecentSubjectRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentSubjectService {

    private final RecentSubjectRepository recentSubjectRepo;

    public List<RecentSubjectDTO> getRecentSubjects(Integer userId, int size) {
        var rows = recentSubjectRepo.findRecentSubjectsByUser(userId, PageRequest.of(0, size));

        return rows.stream().map(r -> RecentSubjectDTO.builder()
                .subjectId(r.getSubjectId())
                .subjectName(r.getSubjectName())
                .subjectCode(r.getSubjectCode())
                .totalDocsInSubject(r.getTotalDocsInSubject() == null ? 0 : r.getTotalDocsInSubject())
                .docsViewedByUser(r.getDocsViewedByUser() == null ? 0 : r.getDocsViewedByUser())
                .lastViewedAt(r.getLastViewedAt())
                .following(false) // chưa hỗ trợ follow
                .build()
        ).toList();
    }
}

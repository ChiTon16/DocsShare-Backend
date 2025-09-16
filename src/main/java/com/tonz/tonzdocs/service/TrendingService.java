package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.dto.projection.TrendingRow;
import com.tonz.tonzdocs.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import java.util.List;

@Service
public class TrendingService {

    private final DocumentRepository repo;

    public TrendingService(DocumentRepository repo) {
        this.repo = repo;
    }

    // helper: Date -> LocalDateTime
    private static LocalDateTime toLocalDateTime(Date d) {
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getSubjectTrending(Integer subjectId, int limit, int offset, int halfLifeHours) {
        List<TrendingRow> rows = repo.findTrendingBySubject(subjectId, halfLifeHours, limit, offset);
        return rows.stream()
                .map(r -> {
                    DocumentDTO dto = new DocumentDTO();
                    dto.setDocumentId(r.getDocumentId());
                    dto.setTitle(r.getTitle());
                    dto.setFilePath(r.getFilePath());

                    // ⭐ FIX: convert Date -> LocalDateTime
                    dto.setUploadTime(toLocalDateTime(r.getUploadTime()));

                    dto.setUserId(r.getUserId());
                    dto.setUserName(r.getUserName());
                    dto.setSubjectId(r.getSubjectId());
                    dto.setSubjectName(r.getSubjectName());
                    dto.setScore(r.getScore() != null ? r.getScore() : null);
                    return dto;
                })
                .toList();
    }
}

package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.DocumentViewDTO;
import com.tonz.tonzdocs.dto.ProgressUpsertRequest;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.RecentViewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.tonz.tonzdocs.dto.ContinueCardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final RecentViewRepository recentViewRepo;
    // Nếu chưa có PdfService, bạn có thể comment dòng này & totalPages = null bên dưới
    private final PdfService pdfService; // optional: đếm số trang

    @Transactional
    public void upsertProgress(User user, Document doc, ProgressUpsertRequest req) {
        var rv = recentViewRepo
                .findByUserUserIdAndDocumentDocumentId(user.getUserId(), doc.getDocumentId())
                .orElseGet(() -> {
                    var n = new RecentView();
                    n.setUser(user); n.setDocument(doc);
                    n.setFirstOpenedAt(LocalDateTime.now());
                    n.setLastPage(1); n.setPercent(0d);
                    return n;
                });

        if (req.lastPage() != null) rv.setLastPage(req.lastPage());
        if (req.percent()  != null) rv.setPercent(Math.min(100.0, Math.max(0.0, req.percent())));
        if (req.sessionReadSeconds() != null) {
            var cur = rv.getTotalReadSeconds() == null ? 0L : rv.getTotalReadSeconds();
            rv.setTotalReadSeconds(cur + req.sessionReadSeconds());
        }
        rv.setViewedAt(LocalDateTime.now());
        recentViewRepo.save(rv);
    }

    /** Trang đang đọc dở (percent < 100) – dữ liệu gọn cho UI ContinueReading */
    public Page<ContinueCardDTO> getContinueCards(User user, Pageable pageable) {
        return recentViewRepo.findContinue(user.getUserId(), pageable)
                .map(rv -> new ContinueCardDTO(
                        rv.getDocument().getDocumentId(),
                        rv.getDocument().getTitle(),
                        rv.getDocument().getFilePath(),
                        rv.getLastPage(),
                        rv.getPercent(),
                        rv.getViewedAt()
                ));
    }

    /** Recently read – dữ liệu gọn cho UI Recently Viewed */
    public Page<ContinueCardDTO> getRecentCards(User user, Pageable pageable) {
        return recentViewRepo.findRecent(user.getUserId(), pageable)
                .map(rv -> new ContinueCardDTO(
                        rv.getDocument().getDocumentId(),
                        rv.getDocument().getTitle(),
                        rv.getDocument().getFilePath(),
                        rv.getLastPage(),
                        rv.getPercent(),
                        rv.getViewedAt()
                ));
    }

    /** Gần đây đã mở (sort theo viewedAt) */
    public Page<DocumentViewDTO> getRecent(User user, Pageable pageable) {
        return recentViewRepo.findRecent(user.getUserId(), pageable)
                .map(rv -> toViewDTO(rv));
    }

    /** Mapper duy nhất để tránh nhầm thứ tự 13 tham số */
    private DocumentViewDTO toViewDTO(RecentView rv) {
        var doc = rv.getDocument();

        // Nếu có PdfService: đếm trang, nếu không thì để null
        Integer totalPages = null;
        try {
            if (pdfService != null && doc != null) {
                totalPages = pdfService.getTotalPages(doc);
            }
        } catch (Exception ignored) {}

        return new DocumentViewDTO(
                doc.getDocumentId(),                      // 1) documentId
                doc.getTitle(),                           // 2) title
                doc.getFilePath(),                        // 3) filePath
                doc.getUploadTime(),                      // 4) uploadTime

                doc.getUser() != null ? doc.getUser().getUserId() : null,  // 5) userId
                doc.getUser() != null ? doc.getUser().getName()   : null,  // 6) userName

                doc.getSubject() != null ? doc.getSubject().getSubjectId() : null, // 7) subjectId
                doc.getSubject() != null ? doc.getSubject().getName()      : null, // 8) subjectName

                rv.getLastPage(),                         // 9)  lastPage
                rv.getPercent(),                          // 10) percent
                rv.getViewedAt(),                         // 11) lastReadAt

                totalPages,                               // 12) totalPages
                "/viewer/" + doc.getDocumentId()          // 13) viewerUrl
        );
    }
}

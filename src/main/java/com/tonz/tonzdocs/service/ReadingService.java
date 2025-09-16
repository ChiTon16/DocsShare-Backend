package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.ContinueCardDTO;
import com.tonz.tonzdocs.dto.DocumentViewDTO;
import com.tonz.tonzdocs.dto.ProgressUpsertRequest;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.RecentViewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final RecentViewRepository recentViewRepo;

    /** Optional – nếu không có bean này, totalPages sẽ để -1 và bỏ qua clamp/convert */
    @Autowired(required = false)
    private PdfService pdfService;

    // ===========================
    // Public APIs
    // ===========================

    /**
     * Upsert tiến độ đọc.
     * - Tạo bản ghi nếu chưa có (set firstOpenedAt = now)
     * - Cập nhật lastPage/percent (tự suy ra nếu thiếu, clamp về khoảng hợp lệ)
     * - Cộng dồn total_read_seconds từ sessionReadSeconds
     * - Cập nhật viewedAt = now
     */
    @Transactional
    public void upsertProgress(User user, Document doc, ProgressUpsertRequest req) {
        RecentView rv = getOrCreate(user, doc);

        final int totalPages = safeTotalPages(doc); // -1 nếu không biết

        // --- chuẩn hoá dữ liệu trang/percent ---
        Integer newLastPage = req.lastPage();
        Double  newPercent  = req.percent();

        // Nếu có tổng số trang, clamp lastPage; nếu thiếu percent thì suy từ lastPage
        if (totalPages > 0) {
            if (newLastPage != null) {
                newLastPage = clampPage(newLastPage, totalPages);
            }
            if (newPercent == null && newLastPage != null) {
                newPercent = percentFromPage(newLastPage, totalPages);
            }
            if (newPercent != null) {
                newPercent = clampPercent(newPercent);
                // Nếu có percent mà thiếu lastPage -> suy ra
                if (newLastPage == null) {
                    newLastPage = pageFromPercent(newPercent, totalPages);
                }
            }
        } else {
            // Không biết tổng số trang -> chỉ clamp percent nếu có
            if (newPercent != null) newPercent = clampPercent(newPercent);
        }

        // --- ghi vào entity ---
        if (newLastPage != null) rv.setLastPage(newLastPage);
        if (newPercent  != null) rv.setPercent(newPercent);

        // Cộng dồn thời gian đọc phiên (bỏ qua số âm/null)
        Long addSec = req.sessionReadSeconds();
        if (addSec != null && addSec > 0) {
            long cur = rv.getTotalReadSeconds() == null ? 0L : rv.getTotalReadSeconds();
            rv.setTotalReadSeconds(cur + addSec);
        }

        // Nếu muốn cho phép FE gửi firstOpenedAt, bỏ comment khối này
        // if (rv.getFirstOpenedAt() == null && req.firstOpenedAt() != null) {
        //     rv.setFirstOpenedAt(req.firstOpenedAt());
        // }

        rv.setViewedAt(LocalDateTime.now());
        recentViewRepo.save(rv);
    }

    /** Trang đang đọc dở (percent < 100) – dữ liệu gọn cho UI Continue Reading */
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

    /** Gần đây đã mở (sort theo viewedAt) – trả về đầy đủ thông tin hiển thị thẻ */
    public Page<DocumentViewDTO> getRecent(User user, Pageable pageable) {
        return recentViewRepo.findRecent(user.getUserId(), pageable)
                .map(this::toViewDTO);
    }

    // ===========================
    // Helpers
    // ===========================

    private RecentView getOrCreate(User user, Document doc) {
        return recentViewRepo
                .findByUserUserIdAndDocumentDocumentId(user.getUserId(), doc.getDocumentId())
                .orElseGet(() -> {
                    RecentView n = new RecentView();
                    n.setUser(user);
                    n.setDocument(doc);
                    n.setFirstOpenedAt(LocalDateTime.now());
                    n.setLastPage(1);
                    n.setPercent(0d);
                    return recentViewRepo.save(n);
                });
    }

    private int safeTotalPages(Document doc) {
        try {
            if (pdfService != null && doc != null) {
                Integer p = pdfService.getTotalPages(doc);
                return (p == null || p < 1) ? -1 : p;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private static int clampPage(int p, int total) {
        if (total < 1) return Math.max(1, p);
        return Math.max(1, Math.min(p, total));
    }

    private static double clampPercent(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0d;
        return Math.max(0d, Math.min(100d, v));
    }

    private static double percentFromPage(int page, int total) {
        if (total <= 0) return 0d;
        // round to 0..100
        return clampPercent(page * 100.0 / total);
    }

    private static int pageFromPercent(double percent, int total) {
        if (total <= 0) return 1;
        int p = (int) Math.round(percent * total / 100.0);
        return clampPage(p, total);
    }

    /** Mapper duy nhất để tránh nhầm thứ tự tham số dài */
    private DocumentViewDTO toViewDTO(RecentView rv) {
        Document doc = rv.getDocument();

        Integer totalPages = null;
        try {
            if (pdfService != null && doc != null) {
                totalPages = pdfService.getTotalPages(doc);
            }
        } catch (Exception ignored) {}

        return new DocumentViewDTO(
                doc.getDocumentId(),                                 // 1) documentId
                doc.getTitle(),                                      // 2) title
                doc.getFilePath(),                                   // 3) filePath
                doc.getUploadTime(),                                 // 4) uploadTime
                doc.getUser() != null ? doc.getUser().getUserId() : null, // 5) userId
                doc.getUser() != null ? doc.getUser().getName()   : null, // 6) userName
                doc.getSubject() != null ? doc.getSubject().getSubjectId() : null, // 7) subjectId
                doc.getSubject() != null ? doc.getSubject().getName()      : null, // 8) subjectName
                rv.getLastPage(),                                    // 9) lastPage
                rv.getPercent(),                                     // 10) percent
                rv.getViewedAt(),                                    // 11) lastReadAt
                totalPages,                                          // 12) totalPages
                "/viewer/" + doc.getDocumentId()                     // 13) viewerUrl
        );
    }
}

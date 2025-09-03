package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.ContinueCardDTO;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingService {
    private final RecentViewRepository recentViewRepo;

    @Transactional
    public void upsertProgress(User user, Document doc, ProgressUpsertRequest req) {
        var rv = recentViewRepo
                .findByUserUserIdAndDocumentDocumentId(user.getUserId(), doc.getDocumentId())
                .orElseGet(() -> {
                    var n = new RecentView();
                    n.setUser(user); n.setDocument(doc);
                    n.setFirstOpenedAt(LocalDateTime.now());
                    return n;
                });

        if (req.lastPage() != null) rv.setLastPage(req.lastPage());
        if (req.percent() != null)  rv.setPercent(Math.min(100.0, Math.max(0.0, req.percent())));
        if (req.sessionReadSeconds() != null) {
            var cur = rv.getTotalReadSeconds() == null ? 0L : rv.getTotalReadSeconds();
            rv.setTotalReadSeconds(cur + req.sessionReadSeconds());
        }
        rv.setViewedAt(LocalDateTime.now());
        recentViewRepo.save(rv);
    }

    public Page<ContinueCardDTO> getContinue(User user, Pageable pageable) {
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

    public Page<ContinueCardDTO> getRecent(User user, Pageable pageable) {
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
}

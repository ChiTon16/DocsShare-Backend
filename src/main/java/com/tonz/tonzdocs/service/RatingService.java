package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.RatingSummaryDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.DocumentRating;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.DocumentRatingRepository;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final DocumentRepository documentRepo;
    private final UserRepository userRepo;
    private final DocumentRatingRepository ratingRepo;

    @Transactional
    public RatingSummaryDTO setRating(Integer userId, Integer documentId, Integer value /* 1|-1|0 */) {
        if (value == null || !(value == 1 || value == -1 || value == 0))
            throw new IllegalArgumentException("value must be 1, -1 or 0");

        Document doc = documentRepo.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var opt = ratingRepo.findByUser_UserIdAndDocument_DocumentId(userId, documentId);

        if (opt.isEmpty()) {
            if (value == 0) return summary(doc, null);      // nothing
            // create new
            DocumentRating r = new DocumentRating();
            r.setDocument(doc);
            r.setUser(user);
            r.setValue(value);
            ratingRepo.save(r);
            if (value == 1) doc.setUpvoteCount(doc.getUpvoteCount() + 1);
            else            doc.setDownvoteCount(doc.getDownvoteCount() + 1);
        } else {
            DocumentRating r = opt.get();
            int old = r.getValue();
            if (value == 0 || value == old) {
                // remove
                if (old == 1) doc.setUpvoteCount(Math.max(0, doc.getUpvoteCount() - 1));
                else          doc.setDownvoteCount(Math.max(0, doc.getDownvoteCount() - 1));
                ratingRepo.delete(r);
                return summary(doc, null);
            } else {
                // switch
                if (old == 1) {
                    doc.setUpvoteCount(Math.max(0, doc.getUpvoteCount() - 1));
                    doc.setDownvoteCount(doc.getDownvoteCount() + 1);
                } else {
                    doc.setDownvoteCount(Math.max(0, doc.getDownvoteCount() - 1));
                    doc.setUpvoteCount(doc.getUpvoteCount() + 1);
                }
                r.setValue(value);
                ratingRepo.save(r);
            }
        }
        documentRepo.save(doc);
        return summary(doc, value == 0 ? null : (value == 1 ? "up" : "down"));
    }

    @Transactional(readOnly = true)
    public RatingSummaryDTO getSummary(Integer userId, Integer documentId) {
        Document doc = documentRepo.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        String my = ratingRepo.findByUser_UserIdAndDocument_DocumentId(userId, documentId)
                .map(r -> r.getValue() == 1 ? "up" : "down")
                .orElse(null);
        return summary(doc, my);
    }

    private RatingSummaryDTO summary(Document doc, String my) {
        long up = doc.getUpvoteCount();       // primitive long -> không null
        long dn = doc.getDownvoteCount();     // primitive long -> không null
        long total = up + dn;

        // Nếu muốn null khi chưa có vote, dùng Integer; nếu field là int thì thay null bằng 0
        Integer percent = (total > 0)
                ? (int) Math.round(up * 100.0 / (double) total)
                : null;

        return new RatingSummaryDTO(up, dn, percent, my);
    }

}

package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.DocumentRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRatingRepository extends JpaRepository<DocumentRating, Long> {
    Optional<DocumentRating> findByUser_UserIdAndDocument_DocumentId(Integer userId, Integer documentId);
    long countByDocument_DocumentIdAndValue(Integer documentId, Integer value); // 1 or -1
}

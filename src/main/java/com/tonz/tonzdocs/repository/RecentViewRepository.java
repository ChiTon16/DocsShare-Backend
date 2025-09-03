package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecentViewRepository extends JpaRepository<RecentView, Integer> {
    boolean existsByUserAndDocument(User user, Document document);

    List<RecentView> findTop10ByUserOrderByViewedAtDesc(User user);

    Optional<RecentView> findByUserUserIdAndDocumentDocumentId(Integer userId, Integer documentId);

    @Query("""
    SELECT rv FROM RecentView rv
     WHERE rv.user.userId = :userId
       AND (rv.percent IS NULL OR rv.percent < 100.0)
     ORDER BY rv.viewedAt DESC
  """)
    Page<RecentView> findContinue(Integer userId, Pageable pageable);

    @Query("""
    SELECT rv FROM RecentView rv
     WHERE rv.user.userId = :userId
     ORDER BY rv.viewedAt DESC
  """)
    Page<RecentView> findRecent(Integer userId, Pageable pageable);
}

package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecentViewRepository extends JpaRepository<RecentView, Integer> {
    boolean existsByUserAndDocument(User user, Document document);

    List<RecentView> findTop10ByUserOrderByViewedAtDesc(User user);
}

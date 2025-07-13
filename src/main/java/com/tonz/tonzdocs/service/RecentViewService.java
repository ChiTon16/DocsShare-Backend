package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.RecentViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecentViewService {

    @Autowired
    private RecentViewRepository recentViewRepo;

    public void saveView(User user, Document document) {
        RecentView view = new RecentView();
        view.setUser(user);
        view.setDocument(document);
        view.setViewedAt(LocalDateTime.now());
        recentViewRepo.save(view);
    }

    public List<RecentView> getRecentViews(User user) {
        return recentViewRepo.findTop10ByUserOrderByViewedAtDesc(user);
    }
}

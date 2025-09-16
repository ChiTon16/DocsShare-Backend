package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.StudyPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyPlanItemRepository extends JpaRepository<StudyPlanItem, Integer> {
    boolean existsByPlan_IdAndDocument_DocumentId(Integer planId, Integer documentId);
    List<StudyPlanItem> findByPlan_IdOrderBySortIndexAscIdAsc(Integer planId);
    void deleteByPlan_IdAndDocument_DocumentId(Integer planId, Integer documentId);
}

// com.tonz.tonzdocs.service.StudyPlanService.java
package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.StudyPlanDTO;
import com.tonz.tonzdocs.dto.StudyPlanItemDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.StudyPlan;
import com.tonz.tonzdocs.model.StudyPlanItem;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.StudyPlanItemRepository;
import com.tonz.tonzdocs.repository.StudyPlanRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyPlanService {
    private final StudyPlanRepository planRepo;
    private final StudyPlanItemRepository itemRepo;
    private final UserRepository userRepo;
    private final DocumentRepository docRepo;

    @Transactional
    public StudyPlanDTO createPlan(Integer userId, String name, String description) {
        var user = userRepo.findById(userId).orElseThrow();
        var plan = StudyPlan.builder()
                .user(user)
                .name(name)
                .description(description)
                .build();
        plan = planRepo.save(plan);
        return toDTO(plan, 0);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanDTO> listPlans(Integer userId) {
        return planRepo.findByUser_UserIdOrderByUpdatedAtDesc(userId).stream()
                .map(p -> toDTO(p, p.getItems().size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudyPlanItemDTO> listItems(Integer userId, Integer planId) {
        planRepo.findByIdAndUser_UserId(planId, userId).orElseThrow();
        return itemRepo.findByPlan_IdOrderBySortIndexAscIdAsc(planId).stream()
                .map(this::toItemDTO)
                .toList();
    }

    @Transactional
    public void addItem(Integer userId, Integer planId, Integer documentId, String note) {
        var plan = planRepo.findByIdAndUser_UserId(planId, userId).orElseThrow();
        if (itemRepo.existsByPlan_IdAndDocument_DocumentId(planId, documentId)) return;

        var doc = docRepo.findById(documentId).orElseThrow();
        int nextIndex = itemRepo.findByPlan_IdOrderBySortIndexAscIdAsc(planId).stream()
                .map(StudyPlanItem::getSortIndex)
                .max(Integer::compareTo).orElse(0) + 1;

        var item = StudyPlanItem.builder()
                .plan(plan)
                .document(doc)
                .note(note)
                .sortIndex(nextIndex)
                .build();
        itemRepo.save(item);
    }

    @Transactional
    public void removeItem(Integer userId, Integer planId, Integer documentId) {
        planRepo.findByIdAndUser_UserId(planId, userId).orElseThrow();
        itemRepo.deleteByPlan_IdAndDocument_DocumentId(planId, documentId);
    }

    // Helpers
    private StudyPlanDTO toDTO(StudyPlan p, int count) {
        return StudyPlanDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .itemsCount(count)
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private StudyPlanItemDTO toItemDTO(StudyPlanItem it) {
        var d = it.getDocument();
        return StudyPlanItemDTO.builder()
                .id(it.getId())
                .documentId(d.getDocumentId())
                .title(d.getTitle())
                .subjectName(d.getSubject() != null ? d.getSubject().getName() : null)
                .userName(d.getUser() != null ? d.getUser().getName() : null)
                .sortIndex(it.getSortIndex())
                .note(it.getNote())
                .build();
    }
}

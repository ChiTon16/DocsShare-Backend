// com.tonz.tonzdocs.repository.StudyPlanRepository.java
package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.StudyPlan;
import com.tonz.tonzdocs.model.StudyPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Integer> {
    List<StudyPlan> findByUser_UserIdOrderByUpdatedAtDesc(Integer userId);
    Optional<StudyPlan> findByIdAndUser_UserId(Integer id, Integer userId);
}

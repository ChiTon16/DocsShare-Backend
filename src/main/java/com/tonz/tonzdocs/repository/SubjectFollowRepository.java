// SubjectFollowRepository.java
package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.model.SubjectFollow;
import com.tonz.tonzdocs.model.SubjectFollowId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectFollowRepository extends JpaRepository<SubjectFollow, SubjectFollowId> {

    boolean existsByUser_UserIdAndSubject_SubjectId(Integer userId, Integer subjectId);

    void deleteByUser_UserIdAndSubject_SubjectId(Integer userId, Integer subjectId);

    @Query("""
        select new com.tonz.tonzdocs.dto.SubjectDTO(s.subjectId, s.name, s.code)
        from SubjectFollow f join f.subject s
        where f.user.userId = :userId
    """)
    List<SubjectDTO> findFollowedDTOByUser(@Param("userId") Integer userId);

    @Query("select count(f) from SubjectFollow f where f.subject.subjectId = :subjectId")
    long countFollowersBySubject(@Param("subjectId") Integer subjectId);
}

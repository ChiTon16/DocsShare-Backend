package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.dto.SubjectDTO;
import com.tonz.tonzdocs.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    Optional<Subject> findByName(String name);

    @Query("""
   select new com.tonz.tonzdocs.dto.SubjectDTO(
      s.subjectId, s.name, s.code
   )
   from Subject s
""")
    List<SubjectDTO> findAllDTO();
}

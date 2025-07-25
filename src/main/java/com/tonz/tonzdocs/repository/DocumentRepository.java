package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByUserUserId(Integer userId);

    List<Document> findByTitleContainingIgnoreCase(String keyword);

    List<Document> findBySubject_SubjectId(Integer subjectId);

}

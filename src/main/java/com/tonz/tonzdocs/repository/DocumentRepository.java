package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer>, JpaSpecificationExecutor<Document> {
    List<Document> findByUserUserId(Integer userId);

    List<Document> findByTitleContainingIgnoreCase(String keyword);

    List<Document> findBySubject_SubjectId(Integer subjectId);

    @Query(value = """
    SELECT
      d.document_id            AS documentId,
      d.title                  AS title,
      d.file_path              AS filePath,
      d.upload_time            AS uploadTime,
      d.user_id                AS userId,
      u.name                   AS userName,
      d.subject_id             AS subjectId,
      s.name                   AS subjectName,
      (
        (LOG10(1 + d.view_count)     * 1.0) +
        (LOG10(1 + d.download_count) * 3.0) +
        (d.upvote_count              * 5.0)
      ) * POW(2, - TIMESTAMPDIFF(HOUR, d.upload_time, NOW()) / :halfLife) AS score
    FROM document d
    JOIN subject s ON s.subject_id = d.subject_id
    JOIN users   u ON u.user_id    = d.user_id
    WHERE d.subject_id = :subjectId
    ORDER BY score DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<com.tonz.tonzdocs.dto.projection.TrendingRow> findTrendingBySubject(
            @Param("subjectId") Integer subjectId,
            @Param("halfLife") int halfLifeHours,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

}

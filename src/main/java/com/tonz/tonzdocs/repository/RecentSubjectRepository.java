// repo/RecentSubjectRepository.java
package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.repository.RecentSubjectRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecentSubjectRepository extends JpaRepository<com.tonz.tonzdocs.model.RecentView, Integer> {

    /**
     * Lấy các môn học mà user đã xem GẦN ĐÂY (thông qua document),
     * kèm: tổng số tài liệu của môn, số tài liệu user đã xem trong môn, và mốc xem gần nhất.
     *
     * Bảng giả định:
     *  recent_views (user_id, document_id, viewed_at, ...)
     *  document (id, subject_id, ...)
     *  subject  (id, name, code, ...)
     */
    @Query(value = """
    SELECT
      s.subject_id AS subjectId,
      s.name       AS subjectName,
      s.code       AS subjectCode,
      -- tổng số tài liệu trong môn
      (SELECT COUNT(*) FROM document d_all WHERE d_all.subject_id = s.subject_id) AS totalDocsInSubject,
      -- số tài liệu user đã mở trong môn (đếm distinct)
      COUNT(DISTINCT d.document_id) AS docsViewedByUser,
      -- mốc xem gần nhất trong môn
      MAX(rv.viewed_at) AS lastViewedAt
    FROM recent_views rv
    JOIN document d  ON d.document_id = rv.document_id
    JOIN subject  s  ON s.subject_id = d.subject_id
    WHERE rv.user_id = :userId
    GROUP BY s.subject_id, s.name, s.code
    ORDER BY MAX(rv.viewed_at) DESC
    """,
            nativeQuery = true)
    List<RecentSubjectRow> findRecentSubjectsByUser(@Param("userId") Integer userId, Pageable pageable);
}

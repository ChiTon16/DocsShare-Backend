// com.tonz.tonzdocs.repository.spec.DocumentSpecs
package com.tonz.tonzdocs.repository.spec;

import com.tonz.tonzdocs.model.Document;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentSpecs {

    /** Tìm theo từ khóa trên title / subject.name / user.name */
    public static Specification<Document> keyword(String q) {
        if (q == null || q.isBlank()) return null;
        final String like = "%" + q.trim().toLowerCase() + "%";
        return (root, cq, cb) -> {
            var subject = root.join("subject", JoinType.LEFT);
            var user    = root.join("user", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(subject.get("name")), like),
                    cb.like(cb.lower(user.get("name")), like)
            );
        };
    }

    public static Specification<Document> subjectId(Integer subjectId) {
        if (subjectId == null) return null;
        return (root, cq, cb) -> cb.equal(root.join("subject", JoinType.LEFT).get("subjectId"), subjectId);
    }

    public static Specification<Document> uploaderId(Integer uploaderId) {
        if (uploaderId == null) return null;
        return (root, cq, cb) -> cb.equal(root.join("user", JoinType.LEFT).get("userId"), uploaderId);
    }

    public static Specification<Document> schoolId(Integer schoolId) {
        if (schoolId == null) return null;
        // Giả sử Document -> User -> School hoặc Document -> Subject -> School (đổi join cho đúng mô hình của bạn)
        return (root, cq, cb) -> cb.equal(root.join("subject", JoinType.LEFT).join("school", JoinType.LEFT).get("schoolId"), schoolId);
    }

    /** Lọc theo năm học (ví dụ lấy từ trường uploadTime hoặc một cột year riêng) */
    public static Specification<Document> year(Integer year) {
        if (year == null) return null;
        return (root, cq, cb) -> cb.equal(cb.function("YEAR", Integer.class, root.get("uploadTime")), year);
    }

    public static Specification<Document> allOf(Specification<Document>... specs) {
        return Specification.allOf(specs);
    }
}

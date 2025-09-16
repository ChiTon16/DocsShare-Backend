package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Root comments theo document
    Page<Comment> findByDocument_DocumentIdAndParentIsNullOrderByPostedAtAsc(Integer documentId, Pageable pageable);

    // Replies theo parent
    Page<Comment> findByParent_CommentIdOrderByPostedAtAsc(Integer parentId, Pageable pageable);

    // Comment của user hiện tại (mới nhất trước)
    Page<Comment> findByUser_UserIdOrderByPostedAtDesc(Integer userId, Pageable pageable);

    long countByDocument_DocumentId(Integer documentId);
    long countByParent_CommentId(Integer parentId);
}

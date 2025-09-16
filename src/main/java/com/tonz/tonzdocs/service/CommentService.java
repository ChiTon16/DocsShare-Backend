package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.CommentCreateRequest;
import com.tonz.tonzdocs.dto.CommentResponse;
import com.tonz.tonzdocs.dto.PageResponse;
import com.tonz.tonzdocs.model.Comment;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.CommentRepository;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    /* ===== Security helpers (thay bằng logic JWT của bạn) ===== */

    private Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("Unauthenticated");

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails u) {
            return u.getUserId(); // dùng thẳng userId
        }
        throw new RuntimeException("Unsupported principal: " + principal.getClass());
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void assertOwnerOrAdmin(Integer ownerUserId) {
        if (isAdmin()) return;
        Integer me = getCurrentUserId();
        if (!me.equals(ownerUserId)) throw new SecurityException("Bạn không có quyền thực hiện hành động này");
    }

    /* ====================== Commands/Queries ====================== */

    @Transactional
    public CommentResponse create(CommentCreateRequest req) {
        Integer me = getCurrentUserId();

        Document doc = documentRepository.findById(req.documentId())
                .orElseThrow(() -> new IllegalArgumentException("Document không tồn tại: " + req.documentId()));

        User user = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + me));

        Comment parent = null;
        if (req.parentId() != null) {
            parent = commentRepository.findById(req.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment không tồn tại: " + req.parentId()));
            // Bảo vệ: parent phải thuộc cùng document
            if (!parent.getDocument().getDocumentId().equals(doc.getDocumentId())) {
                throw new IllegalArgumentException("Parent comment không thuộc cùng document");
            }
        }

        Comment c = Comment.builder()
                .content(req.content())
                .postedAt(LocalDateTime.now())
                .user(user)
                .document(doc)
                .parent(parent)
                .build();

        Comment saved = commentRepository.save(c);
        return toResp(saved);
    }

    /** Root comments (parent=NULL) của 1 document */
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> listRoots(Integer documentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> pg = commentRepository
                .findByDocument_DocumentIdAndParentIsNullOrderByPostedAtAsc(documentId, pageable);
        return toPage(pg);
    }

    /** Replies trực tiếp của 1 comment */
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> listReplies(Integer parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> pg = commentRepository
                .findByParent_CommentIdOrderByPostedAtAsc(parentId, pageable);
        return toPage(pg);
    }

    /** Comment của chính mình */
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> listMine(int page, int size) {
        Integer me = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> pg = commentRepository.findByUser_UserIdOrderByPostedAtDesc(me, pageable);
        return toPage(pg);
    }

    /** Xoá 1 comment (xoá luôn cây replies nhờ orphanRemoval) */
    @Transactional
    public void delete(Integer commentId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment không tồn tại: " + commentId));
        assertOwnerOrAdmin(c.getUser().getUserId());
        commentRepository.delete(c);
    }

    /* ====================== Mapping ====================== */

    private CommentResponse toResp(Comment c) {
        long replyCount = commentRepository.countByParent_CommentId(c.getCommentId());
        return new CommentResponse(
                c.getCommentId(),
                c.getContent(),
                c.getPostedAt(),
                c.getUser() != null ? c.getUser().getUserId() : null,
                c.getUser() != null ? c.getUser().getName() : null,
                c.getDocument() != null ? c.getDocument().getDocumentId() : null,
                c.getDocument() != null ? c.getDocument().getTitle() : null,
                c.getParent() != null ? c.getParent().getCommentId() : null,
                replyCount
        );
        // Nếu lo N+1 do count, có thể bỏ replyCount hoặc thêm batch query/DTO khác cho hiệu năng.
    }

    private PageResponse<CommentResponse> toPage(Page<Comment> pg) {
        return new PageResponse<>(
                pg.map(this::toResp).getContent(),
                pg.getNumber(),
                pg.getSize(),
                pg.getTotalElements(),
                pg.getTotalPages()
        );
    }
}

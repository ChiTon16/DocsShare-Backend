package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.CommentCreateRequest;
import com.tonz.tonzdocs.dto.CommentResponse;
import com.tonz.tonzdocs.dto.PageResponse;
import com.tonz.tonzdocs.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** Tạo comment gốc hoặc reply (nếu có parentId) */
    @PostMapping
    public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentCreateRequest req) {
        return ResponseEntity.ok(commentService.create(req));
    }

    /** Danh sách comment gốc của 1 document (phân trang) */
    @GetMapping("/document/{documentId}/roots")
    public ResponseEntity<PageResponse<CommentResponse>> listRoots(
            @PathVariable Integer documentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.listRoots(documentId, page, size));
    }

    /** Danh sách replies trực tiếp cho 1 comment (phân trang) */
    @GetMapping("/{parentId}/replies")
    public ResponseEntity<PageResponse<CommentResponse>> listReplies(
            @PathVariable Integer parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.listReplies(parentId, page, size));
    }

    /** Comment của chính mình */
    @GetMapping("/me")
    public ResponseEntity<PageResponse<CommentResponse>> listMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.listMine(page, size));
    }

    /** Xoá comment (xoá cả replies nhờ orphanRemoval) */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Integer commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}

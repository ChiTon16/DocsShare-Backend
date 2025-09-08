package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/pub/documents")
@RequiredArgsConstructor
public class PublicDocumentController {

    private final DocumentService documentService;

    /**
     * Trả PNG thumbnail KHÔNG cần Authorization
     * GET /api/pub/documents/{id}/thumbnail?page=1&width=400
     */
    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(
            @PathVariable Integer id,                     // <-- dùng Integer cho khớp service/repo
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "400") int width
    ) {
        try {
            // clamp input để tránh giá trị xấu
            if (page < 1) page = 1;
            if (width <= 0) width = 320;

            byte[] imageBytes = documentService.getThumbnail(id, page, width);

            if (imageBytes == null || imageBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                    .body(imageBytes);

        } catch (IllegalArgumentException notFound) {
            // ví dụ: "Document not found"
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // các lỗi render/IO khác
            return ResponseEntity.status(500).build();
        }
    }
}

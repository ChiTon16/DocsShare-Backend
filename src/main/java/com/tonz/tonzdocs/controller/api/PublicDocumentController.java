package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pub/documents")
public class PublicDocumentController {

    private final DocumentService documentService;

    public PublicDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Trả về thumbnail của tài liệu mà KHÔNG cần Authorization
     * GET /api/public/documents/{id}/thumbnail?page=1&width=400
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "400") int width) {
        try {
            byte[] imageBytes = documentService.getThumbnail(id, page, width);

            if (imageBytes == null || imageBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG); // hoặc JPEG nếu bạn export dạng đó
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

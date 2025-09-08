package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.dto.DocumentViewDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.RecentViewRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.security.CustomUserDetails;
import com.tonz.tonzdocs.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepo;
    private final UserRepository userRepo;
    private final RecentViewRepository recentViewRepo;
    private final DocumentService documentService;

    // ---- Thumbnail: trả THẲNG ảnh PNG (không 302, public/private đều dùng được khi service xử lý) ----
    @GetMapping(value = "/{documentId}/thumbnail", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(
            @PathVariable Integer documentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "400") int width
    ) {
        if (page < 1) page = 1;
        if (width <= 0) width = 320;

        byte[] png = documentService.getThumbnail(documentId, page, width);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(png);
    }

    // ---- OPEN & TRACK: click để mở tài liệu (ghi recent) ----
    @PostMapping("/{id}/open")
    public ResponseEntity<DocumentViewDTO> open(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Integer id
    ) {
        var user = userRepo.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var doc = documentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        var now = LocalDateTime.now();
        var rv = recentViewRepo.findByUserUserIdAndDocumentDocumentId(
                        user.getUserId(), doc.getDocumentId()
                )
                .orElseGet(() -> {
                    var n = new RecentView();
                    n.setUser(user);
                    n.setDocument(doc);
                    n.setFirstOpenedAt(now);
                    n.setLastPage(1);
                    n.setPercent(0d);
                    return n;
                });
        rv.setViewedAt(now);
        recentViewRepo.save(rv);

        // totalPages có thể tính bằng PdfBox nếu bạn đã có PdfService; để null cho nhẹ nhàng.
        Integer totalPages = null;

        var dto = new DocumentViewDTO(
                doc.getDocumentId(),
                doc.getTitle(),
                doc.getFilePath(),
                doc.getUploadTime(),
                doc.getUser() != null ? doc.getUser().getUserId() : null,
                doc.getUser() != null ? doc.getUser().getName() : null,
                doc.getSubject() != null ? doc.getSubject().getSubjectId() : null,
                doc.getSubject() != null ? doc.getSubject().getName() : null,
                rv.getLastPage(),
                rv.getPercent(),
                rv.getViewedAt(),
                totalPages,
                "/viewer/" + doc.getDocumentId()
        );
        return ResponseEntity.ok(dto);
    }

    // ---- STREAM PDF: phục vụ iframe/PDF.js, hỗ trợ Range ----
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> streamPdf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Integer id,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) throws IOException {

        var doc = documentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        var path = doc.getFilePath();
        if (path == null || path.isBlank()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        // ===== HTTP/HTTPS (public) =====
        if (path.startsWith("http://") || path.startsWith("https://")) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(id + ".pdf").build());
            headers.set("Accept-Ranges", "bytes");

            var conn = documentService.openHttp(path, rangeHeader);
            int code = conn.getResponseCode();
            int status = (code == 206) ? 206 : 200;

            long contentLength = conn.getContentLengthLong();
            String contentRange = conn.getHeaderField("Content-Range");
            if (contentRange != null) headers.set("Content-Range", contentRange);
            if (contentLength >= 0) headers.setContentLength(contentLength);

            InputStream in = (code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            if (in == null) in = conn.getInputStream();

            return new ResponseEntity<>(new InputStreamResource(in), headers,
                    (status == 206) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
        }

        // ===== CASE 2: LOCAL FILE =====
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        final long fileLength = file.length();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(file.getName()).build());
        headers.set("Accept-Ranges", "bytes");

        if (rangeHeader == null || rangeHeader.isBlank()) {
            headers.setContentLength(fileLength);
            return new ResponseEntity<>(
                    new InputStreamResource(new FileInputStream(file)),
                    headers,
                    HttpStatus.OK
            );
        }

        // Parse range
        java.util.List<org.springframework.http.HttpRange> ranges;
        try {
            ranges = org.springframework.http.HttpRange.parseRanges(rangeHeader);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + fileLength)
                    .build();
        }
        if (ranges.isEmpty()) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + fileLength)
                    .build();
        }

        var range = ranges.get(0);
        long start = range.getRangeStart(fileLength);
        long end   = range.getRangeEnd(fileLength);
        if (start >= fileLength || start < 0 || end < start) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + fileLength)
                    .build();
        }

        final long rangeStart = start;
        final long rangeEnd   = end;
        final long contentLength = rangeEnd - rangeStart + 1;

        final RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(rangeStart);
        InputStream limited = new InputStream() {
            long pos = rangeStart;
            @Override public int read() throws IOException {
                if (pos > rangeEnd) return -1;
                pos++;
                return raf.read();
            }
            @Override public int read(byte[] b, int off, int len) throws IOException {
                long remaining = rangeEnd - pos + 1;
                if (remaining <= 0) return -1;
                int toRead = (int) Math.min(len, remaining);
                int r = raf.read(b, off, toRead);
                if (r > 0) pos += r;
                return r;
            }
            @Override public void close() throws IOException { raf.close(); }
        };

        headers.setContentLength(contentLength);
        headers.set("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        return new ResponseEntity<>(new InputStreamResource(limited), headers, HttpStatus.PARTIAL_CONTENT);
    }



    // ---- Metadata ----
    @GetMapping
    public List<DocumentDTO> getAllDocuments() {
        return documentRepo.findAll().stream()
                .map(DocumentService::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentMeta(@PathVariable Integer id) {
        return documentRepo.findById(id)
                .<ResponseEntity<?>>map(doc -> ResponseEntity.ok(DocumentService.toDTO(doc)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy tài liệu")));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(@RequestParam("q") String keyword) {
        var results = documentRepo.findByTitleContainingIgnoreCase(keyword);
        var dtos = results.stream().map(DocumentService::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

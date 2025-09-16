package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.dto.DocumentViewDTO;
import com.tonz.tonzdocs.dto.RatingSummaryDTO;
import com.tonz.tonzdocs.model.RecentView;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.repository.RecentViewRepository;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.security.CustomUserDetails;
import com.tonz.tonzdocs.service.DocumentService;
import com.tonz.tonzdocs.service.RatingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tonz.tonzdocs.repository.spec.DocumentSpecs.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepo;
    private final UserRepository userRepo;
    private final RecentViewRepository recentViewRepo;
    private final DocumentService documentService;
    private final RatingService ratingService;

    /** Helper: lấy userId từ principal hoặc null nếu anonymous */
    private Integer extractUserId(Object principal) {
        if (principal instanceof CustomUserDetails p) return p.getUserId();
        return null;
    }

    // ---- Thumbnail ----
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

    // ---- OPEN & TRACK (GET/POST đều được) ----
    @RequestMapping(value = "/{id}/open", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<DocumentViewDTO> open(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer id
    ) {
        var doc = documentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        Integer userId = extractUserId(principal);
        var now = LocalDateTime.now();

        Integer lastPage = null;
        Double  percent  = null;
        LocalDateTime viewedAt = null;

        if (userId != null) {
            var user = userRepo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            var rv = recentViewRepo.findByUserUserIdAndDocumentDocumentId(user.getUserId(), doc.getDocumentId())
                    .orElseGet(() -> {
                        var n = new RecentView();
                        n.setUser(user);
                        n.setDocument(doc);
                        n.setFirstOpenedAt(now);
                        n.setLastPage(1);
                        n.setPercent(0d);
                        return n;
                    });

            final long VIEW_GAP_MINUTES = 10;
            LocalDateTime lastViewed = rv.getViewedAt();
            boolean shouldIncreaseView =
                    (lastViewed == null) || Duration.between(lastViewed, now).toMinutes() >= VIEW_GAP_MINUTES;

            if (shouldIncreaseView) {
                doc.setViewCount(doc.getViewCount() + 1);
                documentRepo.save(doc);
            }

            rv.setViewedAt(now);
            recentViewRepo.save(rv);

            lastPage = rv.getLastPage();
            percent  = rv.getPercent();
            viewedAt = rv.getViewedAt();
        } else {
            // anonymous: tăng view luôn (hoặc tùy bạn throttle theo cookie)
            doc.setViewCount(doc.getViewCount() + 1);
            documentRepo.save(doc);
        }

        Integer totalPages = null; // nếu có thể tính thì set

        var dto = new DocumentViewDTO(
                doc.getDocumentId(),
                doc.getTitle(),
                doc.getFilePath(),
                doc.getUploadTime(),
                doc.getUser() != null ? doc.getUser().getUserId() : null,
                doc.getUser() != null ? doc.getUser().getName() : null,
                doc.getSubject() != null ? doc.getSubject().getSubjectId() : null,
                doc.getSubject() != null ? doc.getSubject().getName() : null,
                lastPage,
                percent,
                viewedAt,
                totalPages,
                "/viewer/" + doc.getDocumentId()
        );
        return ResponseEntity.ok(dto);
    }

    // ---- STREAM PDF: hỗ trợ Range (giữ code trong controller) ----
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> streamPdf(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer id,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) throws IOException {

        var doc = documentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        var path = doc.getFilePath();
        if (path == null || path.isBlank()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        // ===== CASE 1: HTTP/HTTPS (public) =====
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

        // Parse Range
        List<HttpRange> ranges;
        try {
            ranges = HttpRange.parseRanges(rangeHeader);
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
    public ResponseEntity<?> searchDocuments(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "uploaderId", required = false) Integer uploaderId,
            @RequestParam(value = "schoolId", required = false) Integer schoolId,
            @RequestParam(value = "year", required = false) Integer year,
            // phân trang + sắp xếp
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "uploadTime,desc") String sort
    ) {
        // Parse sort: "field,dir"
        Sort sortObj;
        try {
            String[] parts = sort.split(",", 2);
            String field = parts[0];
            String dir   = (parts.length > 1 ? parts[1] : "asc");
            sortObj = "desc".equalsIgnoreCase(dir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        } catch (Exception e) {
            sortObj = Sort.by("uploadTime").descending();
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), sortObj);

        var spec = allOf(
                keyword(q),
                subjectId(subjectId),
                uploaderId(uploaderId),
                schoolId(schoolId),
                year(year)
        );

        Page<DocumentDTO> result = documentRepo.findAll(spec, pageable)
                .map(DocumentService::toDTO);

        return ResponseEntity.ok(Map.of(
                "content", result.getContent(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "sort", sortObj.toString()
        ));
    }

    // ---- Rating ----
    @GetMapping("/{id}/rating")
    public ResponseEntity<RatingSummaryDTO> getRatingSummary(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer id
    ) {
        Integer userId = extractUserId(principal);
        RatingSummaryDTO dto = (userId != null)
                ? ratingService.getSummary(userId, id)
                : ratingService.getSummary(-1, id);
        return ResponseEntity.ok(dto);
    }

    @Data
    static class RateRequest { private String action; }

    @PostMapping("/{id}/rating")
    public ResponseEntity<RatingSummaryDTO> rate(
            @AuthenticationPrincipal Object principal,
            @PathVariable Integer id,
            @RequestBody RateRequest req
    ) {
        Integer userId = extractUserId(principal);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        int v = switch (req.getAction() == null ? "" : req.getAction().toLowerCase()) {
            case "up", "upvote", "like" -> 1;
            case "down", "downvote", "dislike" -> -1;
            case "clear", "remove", "" -> 0;
            default -> throw new IllegalArgumentException("action must be up | down | clear");
        };
        return ResponseEntity.ok(ratingService.setRating(userId, id, v));
    }
}

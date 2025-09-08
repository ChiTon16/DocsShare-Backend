    package com.tonz.tonzdocs.controller.api;

    import com.tonz.tonzdocs.dto.ContinueCardDTO;
    import com.tonz.tonzdocs.dto.ProgressUpsertRequest;
    import com.tonz.tonzdocs.dto.DocumentDTO;
    import com.tonz.tonzdocs.model.Document;
    import com.tonz.tonzdocs.model.User;
    import com.tonz.tonzdocs.repository.DocumentRepository;
    import com.tonz.tonzdocs.repository.UserRepository;
    import com.tonz.tonzdocs.service.DocumentService;
    import com.tonz.tonzdocs.service.ReadingService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.stream.Collectors;

    @RestController
    @RequestMapping("/api/users")
    public class UserDocumentController {

        @Autowired
        private DocumentRepository documentRepo;

        @Autowired
        private UserRepository userRepo;

        @Autowired
        private ReadingService readingService;

        // ================== TÀI LIỆU CỦA USER (đang có) ==================
        @GetMapping("/{userId}/documents")
        public ResponseEntity<?> getDocumentsByUser(@PathVariable Integer userId) {
            List<Document> documents = documentRepo.findByUserUserId(userId);
            if (documents.isEmpty()) return ResponseEntity.noContent().build();

            List<DocumentDTO> dtos = documents.stream()
                    .map(DocumentService::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }

        // ================== CẬP NHẬT TIẾN ĐỘ ĐỌC ==================
        // Client gọi khi user mở/đổi trang/đóng viewer
        @PostMapping("/{userId}/reading/progress")
        public ResponseEntity<Void> upsertProgress(
                @PathVariable Integer userId,
                @RequestBody ProgressUpsertRequest req
        ) {
            // đảm bảo document thuộc hệ thống
            Document doc = documentRepo.findById(req.documentId())
                    .orElse(null);
            if (doc == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            // lấy user
            User user = userRepo.findById(userId)
                    .orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            // cập nhật/ghi nhận tiến độ
            readingService.upsertProgress(user, doc, req);
            return ResponseEntity.ok().build();
        }

        // ================== DANH SÁCH "CONTINUE READING" ==================
        @GetMapping("/{userId}/reading/continue")
        public ResponseEntity<Page<ContinueCardDTO>> getContinueReading(
                @PathVariable Integer userId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "12") int size
        ) {
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            Page<ContinueCardDTO> result =
                    readingService.getContinueCards(user, PageRequest.of(page, size));
            return ResponseEntity.ok(result);
        }

        // ================== DANH SÁCH "RECENTLY READ" ==================
        @GetMapping("/{userId}/reading/recent")
        public ResponseEntity<Page<ContinueCardDTO>> getRecentlyRead(
                @PathVariable Integer userId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "12") int size
        ) {
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            Page<ContinueCardDTO> result =
                    readingService.getRecentCards(user, PageRequest.of(page, size));
            return ResponseEntity.ok(result);
        }

    }

package com.tonz.tonzdocs.controller.api;

import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.service.UploadService;
import com.tonz.tonzdocs.repository.DocumentRepository;
import com.tonz.tonzdocs.model.User;
import com.tonz.tonzdocs.repository.UserRepository;
import com.tonz.tonzdocs.model.Subject;
import com.tonz.tonzdocs.repository.SubjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SubjectRepository subjectRepo;

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("userId") Integer userId,
            @RequestParam("subjectId") Integer subjectId
    ) {
        try {
            // 1. Upload file lên Cloudinary → lấy URL
            String url = uploadService.uploadFile(file);

            // 2. Lấy user từ DB
            Optional<User> userOpt = userRepo.findById(userId);
            if (userOpt.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "User không tồn tại"));

            // 3. Lấy subject từ DB
            Optional<Subject> subjectOpt = subjectRepo.findById(subjectId);
            if (subjectOpt.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Môn học không tồn tại"));

            // 4. Tạo đối tượng Document và lưu vào DB
            Document doc = new Document();
            doc.setTitle(title);
            doc.setFilePath(url);
            doc.setUploadTime(LocalDateTime.now());
            doc.setUser(userOpt.get());
            doc.setSubject(subjectOpt.get());

            documentRepo.save(doc);

            // 5. Trả về kết quả
            return ResponseEntity.ok(Map.of(
                    "message", "Upload và lưu tài liệu thành công!",
                    "url", url
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Upload thất bại",
                    "details", e.getMessage()
            ));
        }
    }
}

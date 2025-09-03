package com.tonz.tonzdocs.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;                  // 👈 thêm import
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 👈 thêm import
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class UploadService {

    @Autowired
    private Cloudinary cloudinary;

    // 👇 cấu hình thư mục lưu avatar (có thể setup trong application.yml/properties)
    @Value("${cloudinary.avatar-folder:yubeldocs/avatars}")
    private String avatarFolder;

    /**
     * Upload file nói chung (giữ nguyên method cũ của bạn).
     * Cloudinary sẽ tự suy đoán resource_type.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Upload ảnh với params tuỳ chỉnh (folder, public_id, overwrite...).
     */
    public String uploadImage(MultipartFile image, String folder, String publicId) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is empty");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        Map<String, Object> params = ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,          // ví dụ: user_123
                "overwrite", true,
                "resource_type", "image",
                "unique_filename", false,
                "use_filename", false,
                // Tuỳ chọn: ép avatar thành ảnh vuông 256x256 (fill + auto gravity)
                "transformation", new Transformation().width(256).height(256).crop("fill").gravity("auto")
        );

        Map uploadResult = cloudinary.uploader().upload(image.getBytes(), params);
        return (String) uploadResult.get("secure_url");   // luôn dùng https
    }

    /**
     * Upload avatar user vào folder cấu hình sẵn.
     * @param avatar   file ảnh
     * @param publicId tên public_id mong muốn (vd: "user_123")
     */
    public String uploadAvatar(MultipartFile avatar, String publicId) throws IOException {
        return uploadImage(avatar, avatarFolder, publicId);
    }

    /**
     * (Tuỳ chọn) Xoá ảnh theo public_id (khi user thay avatar).
     */
    public void deleteByPublicId(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
    }
}

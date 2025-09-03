package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final Cloudinary cloudinary; // cần cấu hình bean Cloudinary

    // ===== DTO mapper giữ nguyên =====
    public static DocumentDTO toDTO(Document doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.setDocumentId(doc.getDocumentId());
        dto.setTitle(doc.getTitle());
        dto.setFilePath(doc.getFilePath());
        dto.setUploadTime(doc.getUploadTime());
        if (doc.getUser() != null) {
            dto.setUserId(doc.getUser().getUserId());
            dto.setUserName(doc.getUser().getName());
        }
        if (doc.getSubject() != null) {
            dto.setSubjectId(doc.getSubject().getSubjectId());
            dto.setSubjectName(doc.getSubject().getName());
        }
        return dto;
    }

    /**
     * Trả về PNG bytes của thumbnail.
     * - Nếu filePath là Cloudinary: tạo SIGNED URL transform (page,width → PNG) rồi tải bytes → trả về.
     * - Ngược lại:
     *    + Nếu là PDF: dùng PDFBox render → scale → PNG.
     *    + Nếu là ảnh: đọc → scale → PNG.
     */
    public byte[] getThumbnail(Long documentId, int page, int width) {
        Document doc = documentRepository.findById(documentId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        String filePath = doc.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException("Document has no filePath");
        }

        try {
            // 1) Trường hợp Cloudinary: dùng signed URL để tránh 401
            if (isCloudinaryUrl(filePath)) {
                String signed = buildCloudinarySignedThumbUrl(filePath, page, width);
                return httpGetBytes(signed); // đây là PNG đã transform
            }

            // 2) Không phải Cloudinary: xử lý như thường
            String lower = filePath.toLowerCase(Locale.ROOT);
            BufferedImage thumb;

            if (lower.endsWith(".pdf")) {
                byte[] pdfBytes = readAllBytes(filePath);
                try (PDDocument pdf = PDDocument.load(pdfBytes)) {
                    int pageIndex = Math.max(1, page) - 1;
                    pageIndex = Math.min(pageIndex, pdf.getNumberOfPages() - 1);
                    PDFRenderer renderer = new PDFRenderer(pdf);
                    BufferedImage rendered = renderer.renderImageWithDPI(pageIndex, 150, ImageType.RGB);
                    thumb = scaleToWidth(rendered, width);
                }
            } else {
                BufferedImage original = readImage(filePath);
                if (original == null) {
                    throw new IllegalStateException("Unsupported image format or not found: " + filePath);
                }
                thumb = scaleToWidth(original, width);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumb, "png", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed generating thumbnail: " + e.getMessage(), e);
        }
    }

    // ===== Helpers =====

    private static boolean isHttp(String s) {
        return s.startsWith("http://") || s.startsWith("https://");
    }

    private static boolean isCloudinaryUrl(String s) {
        if (!isHttp(s)) return false;
        try {
            String host = new URI(s).getHost();
            return host != null && host.endsWith("res.cloudinary.com");
        } catch (Exception ignore) {
            return false;
        }
    }

    /** Tạo signed URL Cloudinary xuất PNG của 1 trang PDF với width mong muốn */
    private String buildCloudinarySignedThumbUrl(String fileUrl, int page, int width) {
        CloudParts p = parseCloudinaryUrl(fileUrl);
        if (page < 1) page = 1;
        if (width <= 0) width = 320;

        com.cloudinary.Url urlBuilder = cloudinary.url()
                .secure(true)
                .resourceType(p.resourceType)      // "image" | "video" | "raw" (PDF thường là image)
                .type(p.deliveryType)              // "upload" | "authenticated" | ...
                .transformation(
                        new Transformation<>()
                                .page(page)
                                .crop("scale").width(width)
                                .fetchFormat("png")
                )
                .signed(true);

        return urlBuilder.generate(p.publicId);   // trả về URL PNG đã ký
    }

    /** Tải toàn bộ bytes từ URL (PNG đã transform) */
    private static byte[] httpGetBytes(String url) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
            return is.readAllBytes();
        }
    }

    private static byte[] readAllBytes(String path) throws IOException {
        if (isHttp(path)) return httpGetBytes(path);
        return Files.readAllBytes(Path.of(path));
    }

    private static BufferedImage readImage(String path) throws IOException {
        if (isHttp(path)) {
            try (InputStream is = new URL(path).openStream()) {
                return ImageIO.read(is);
            }
        }
        try (InputStream is = Files.newInputStream(Path.of(path))) {
            return ImageIO.read(is);
        }
    }

    private static BufferedImage scaleToWidth(BufferedImage src, int targetW) {
        if (targetW <= 0) targetW = 320;
        int w = src.getWidth(), h = src.getHeight();
        int targetH = Math.max(1, (int) Math.round(h * (targetW / (double) w)));

        BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();
        return dst;
    }

    // ===== Parse Cloudinary URL =====
    private static class CloudParts {
        final String resourceType; // image|video|raw
        final String deliveryType; // upload|authenticated|private|...
        final String publicId;     // folder/file (không đuôi)
        CloudParts(String r, String d, String p){resourceType=r; deliveryType=d; publicId=p;}
    }

    /** Parse dạng .../<resource_type>/<delivery_type>/v123/folder/file.ext */
    private static CloudParts parseCloudinaryUrl(String fileUrl) {
        URI uri = URI.create(fileUrl);
        String[] seg = uri.getPath().split("/");
        int iUpload = -1;
        for (int i = 0; i < seg.length; i++) {
            if ("upload".equals(seg[i]) || "authenticated".equals(seg[i]) || "private".equals(seg[i])) {
                iUpload = i; break;
            }
        }
        if (iUpload < 2) throw new IllegalArgumentException("Not a valid Cloudinary URL: " + fileUrl);
        String resourceType = seg[iUpload - 1];
        String deliveryType = seg[iUpload];

        StringBuilder after = new StringBuilder();
        for (int i = iUpload + 1; i < seg.length; i++) {
            if (!seg[i].isEmpty()) after.append(seg[i]).append("/");
        }
        String afterStr = after.toString();
        // bỏ v123/
        if (afterStr.startsWith("v") && afterStr.indexOf('/') > 0 &&
                afterStr.substring(1, afterStr.indexOf('/')).matches("\\d+")) {
            afterStr = afterStr.substring(afterStr.indexOf('/') + 1);
        }
        if (afterStr.endsWith("/")) afterStr = afterStr.substring(0, afterStr.length() - 1);
        String decoded = URLDecoder.decode(afterStr, StandardCharsets.UTF_8);
        int dot = decoded.lastIndexOf('.');
        String publicId = (dot > 0 ? decoded.substring(0, dot) : decoded);

        return new CloudParts(resourceType, deliveryType, publicId);
    }
}

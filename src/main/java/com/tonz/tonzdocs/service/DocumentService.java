// src/main/java/com/tonz/tonzdocs/service/DocumentService.java
package com.tonz.tonzdocs.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final Cloudinary cloudinary;

    // ====== map DTO (giữ nguyên) ======
    public static DocumentDTO toDTO(Document doc) {
        var dto = new DocumentDTO();
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

    // ========= PUBLIC thumbnail =========
    public byte[] getThumbnail(Integer documentId, int page, int width) {
        Document doc = documentRepository.findById(documentId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        String filePath = doc.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException("Document has no filePath");
        }

        try {
            if (isCloudinaryUrl(filePath)) {
                // Cloudinary PUBLIC: KHÔNG ký, transform trực tiếp
                CloudParts p = parseCloudinaryUrl(filePath);
                if (page < 1) page = 1;
                if (width <= 0) width = 320;

                var url = cloudinary.url()
                        .secure(true)
                        .resourceType(p.resourceType)   // với PDF nên là "image"
                        .type("upload")                 // PUBLIC delivery
                        .transformation(new Transformation<>()
                                .page(page)
                                .crop("scale").width(width)
                                .fetchFormat("png")
                        );

                String thumb = url.generate(p.publicId); // không .signed(true)
                return httpGetBytes(thumb);
            }

            // Không phải Cloudinary → đọc local/URL thường
            String lower = filePath.toLowerCase(Locale.ROOT);
            BufferedImage img;
            if (lower.endsWith(".pdf")) {
                byte[] bytes = readAll(filePath);
                try (PDDocument pdf = PDDocument.load(bytes)) {
                    int idx = Math.max(0, Math.min(page - 1, pdf.getNumberOfPages() - 1));
                    BufferedImage rendered = new PDFRenderer(pdf).renderImageWithDPI(idx, 150, ImageType.RGB);
                    img = scaleToWidth(rendered, width);
                }
            } else {
                BufferedImage original = readImage(filePath);
                if (original == null) throw new IllegalStateException("Unsupported image: " + filePath);
                img = scaleToWidth(original, width);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Generate thumbnail failed: " + e.getMessage(), e);
        }
    }

    // ========= HTTP open (public) =========
    public HttpURLConnection openHttp(String fileUrl, String rangeHeader) throws IOException {
        var u = new URL(fileUrl);
        var c = (HttpURLConnection) u.openConnection();
        c.setInstanceFollowRedirects(true);
        if (rangeHeader != null && !rangeHeader.isBlank()) {
            c.setRequestProperty("Range", rangeHeader);
        }
        c.connect();
        return c;
    }

    // ===== helpers =====
    private static boolean isCloudinaryUrl(String s) {
        if (s == null) return false;
        try { return new URI(s).getHost().endsWith("res.cloudinary.com"); }
        catch (Exception e) { return false; }
    }

    // /<resource_type>/<delivery_type>/v123/folder/file.ext
    static class CloudParts {
        final String resourceType, deliveryType, publicId;
        CloudParts(String r, String d, String p){resourceType=r; deliveryType=d; publicId=p;}
    }
    private static CloudParts parseCloudinaryUrl(String url) {
        URI uri = URI.create(url);
        String[] seg = uri.getPath().split("/");
        int iType = -1;
        for (int i = 0; i < seg.length; i++) {
            if ("upload".equals(seg[i]) || "authenticated".equals(seg[i]) || "private".equals(seg[i])) { iType = i; break; }
        }
        if (iType < 2) throw new IllegalArgumentException("Not a cloudinary URL: " + url);
        String resourceType = seg[iType - 1];
        String deliveryType = "upload"; // mặc định public
        // nếu trước đó là authenticated/private, ta vẫn tạo thumb public → type=upload
        StringBuilder after = new StringBuilder();
        for (int i = iType + 1; i < seg.length; i++) if (!seg[i].isEmpty()) after.append(seg[i]).append("/");
        String rest = after.toString();
        if (rest.startsWith("v") && rest.indexOf('/') > 0 && rest.substring(1, rest.indexOf('/')).matches("\\d+")) {
            rest = rest.substring(rest.indexOf('/') + 1);
        }
        if (rest.endsWith("/")) rest = rest.substring(0, rest.length() - 1);
        int dot = rest.lastIndexOf('.');
        String pid = (dot > 0 ? rest.substring(0, dot) : rest);
        return new CloudParts(resourceType, deliveryType, pid);
    }

    private static byte[] httpGetBytes(String url) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
            return is.readAllBytes();
        }
    }
    private static byte[] readAll(String path) throws IOException {
        if (path.startsWith("http")) return httpGetBytes(path);
        return Files.readAllBytes(Path.of(path));
    }
    private static BufferedImage readImage(String path) throws IOException {
        InputStream is = path.startsWith("http")
                ? new URL(path).openStream()
                : Files.newInputStream(Path.of(path));
        try (is) { return ImageIO.read(is); }
    }
    private static BufferedImage scaleToWidth(BufferedImage src, int w) {
        if (w <= 0) w = 320;
        int h = Math.max(1, (int) Math.round(src.getHeight() * (w / (double) src.getWidth())));
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }
}

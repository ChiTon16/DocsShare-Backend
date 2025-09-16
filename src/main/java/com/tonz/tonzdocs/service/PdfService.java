// src/main/java/com/tonz/tonzdocs/service/PdfService.java
package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.model.Document;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@Service
@RequiredArgsConstructor
public class PdfService {

    /** Giữ nguyên: lấy file local nếu bạn lưu filePath là đường dẫn trên máy chủ */
    public File getPdfFile(Document doc) {
        return new File(doc.getFilePath());
    }

    // ====== MỚI: helper mở InputStream cho cả local và http/https ======

    /** Chuẩn hoá path: đổi '\' -> '/', fix "https:/" -> "https://", "http:/" -> "http://" */
    private String normalizePath(String raw) {
        if (raw == null) return null;
        String p = raw.trim().replace('\\', '/');
        if (p.startsWith("https:/") && !p.startsWith("https://")) {
            p = "https://" + p.substring("https:/".length());
        } else if (p.startsWith("http:/") && !p.startsWith("http://")) {
            p = "http://" + p.substring("http:/".length());
        }
        return p;
    }

    private boolean isHttpUrl(String p) {
        return p != null && (p.startsWith("http://") || p.startsWith("https://"));
    }

    /** Mở stream từ filePath (hỗ trợ http/https & local) */
    private InputStream openStream(Document doc) throws IOException {
        String path = normalizePath(doc.getFilePath());
        if (path == null || path.isBlank()) {
            throw new FileNotFoundException("Empty filePath");
        }

        if (isHttpUrl(path)) {
            URLConnection c = new URL(path).openConnection();
            c.setConnectTimeout(5000);
            c.setReadTimeout(15000);
            return c.getInputStream();
        }

        // local file
        return new FileInputStream(new File(path));
    }

    // ====== CẬP NHẬT: dùng openStream; nếu lỗi fallback về cách cũ ======

    /** Đếm số trang PDF (hỗ trợ cả URL lẫn local). Giữ nguyên signature. */
    public int getTotalPages(Document doc) {
        // Thử cách mới: mở stream (đọc được URL)
        try (InputStream in = openStream(doc); PDDocument pd = PDDocument.load(in)) {
            return pd.getNumberOfPages();
        } catch (Exception e) {
            // Fallback cách cũ để không phá code hiện hữu
            try (var fis = new FileInputStream(getPdfFile(doc)); var pd = PDDocument.load(fis)) {
                return pd.getNumberOfPages();
            } catch (Exception ex) {
                ex.printStackTrace();
                return 1; // fallback an toàn
            }
        }
    }

    /** Render 1 trang thành ảnh (hỗ trợ cả URL lẫn local). Giữ nguyên signature. */
    public BufferedImage renderPage(Document doc, int pageIndex, int width) throws Exception {
        // Thử cách mới
        try (InputStream in = openStream(doc); PDDocument pd = PDDocument.load(in)) {
            return renderWithWidth(pd, pageIndex, width);
        } catch (Exception e) {
            // Fallback cách cũ
            try (var fis = new FileInputStream(getPdfFile(doc)); var pd = PDDocument.load(fis)) {
                return renderWithWidth(pd, pageIndex, width);
            }
        }
    }

    // Helper dùng chung cho 2 nhánh trên
    private BufferedImage renderWithWidth(PDDocument pd, int pageIndex, int width) throws IOException {
        var renderer = new PDFRenderer(pd);
        var img = renderer.renderImageWithDPI(pageIndex, 144);
        if (width > 0 && img.getWidth() != width) {
            int h = (int) Math.round(img.getHeight() * (width / (double) img.getWidth()));
            var scaled = new BufferedImage(width, h, BufferedImage.TYPE_INT_RGB);
            var g = scaled.createGraphics();
            g.drawImage(img, 0, 0, width, h, null);
            g.dispose();
            return scaled;
        }
        return img;
    }
}

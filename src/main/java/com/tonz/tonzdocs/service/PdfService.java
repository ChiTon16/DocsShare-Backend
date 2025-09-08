// src/main/java/com/tonz/tonzdocs/service/PdfService.java
package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.model.Document;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

@Service
@RequiredArgsConstructor
public class PdfService {

    /** Lấy file PDF thật từ Document (tuỳ cách bạn lưu path/filePath) */
    public File getPdfFile(Document doc) {
        // nếu bạn lưu đường dẫn tuyệt đối thì dùng trực tiếp
        return new File(doc.getFilePath());
    }

    /** Đếm số trang PDF */
    public int getTotalPages(Document doc) {
        try (var fis = new FileInputStream(getPdfFile(doc));
             var pd = PDDocument.load(fis)) {
            return pd.getNumberOfPages();
        } catch (Exception e) {
            e.printStackTrace();
            return 1; // fallback
        }
    }

    /** Render thumbnail một trang (BufferedImage) */
    public BufferedImage renderPage(Document doc, int pageIndex, int width) throws Exception {
        try (var fis = new FileInputStream(getPdfFile(doc));
             var pd = PDDocument.load(fis)) {
            var renderer = new PDFRenderer(pd);
            var img = renderer.renderImageWithDPI(pageIndex, 144); // 144 dpi
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
}

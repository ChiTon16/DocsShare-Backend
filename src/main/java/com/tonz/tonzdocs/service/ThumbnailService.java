package com.tonz.tonzdocs.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

@Service
public class ThumbnailService {

    /* ===== Public API ===== */
    public byte[] renderFromFilePath(String filePath, int page, int width) throws IOException {
        String lower = filePath.toLowerCase();

        if (isPdfPath(lower)) {
            return renderPdfPage(filePath, page, width);
        } else if (isImagePath(lower)) {
            BufferedImage img = readImage(filePath);
            return toPngScaled(img, width);
        } else {
            throw new IOException("Unsupported media type: " + filePath);
        }
    }

    /* ===== PDF ===== */
    public byte[] renderPdfPage(String filePath, int page, int width) throws IOException {
        if (isRemote(filePath)) {
            try (InputStream in = new BufferedInputStream(new URL(filePath).openStream());
                 PDDocument doc = PDDocument.load(in)) {
                return renderPdfDocToPng(doc, page, width);
            }
        } else {
            try (PDDocument doc = PDDocument.load(Path.of(filePath).toFile())) {
                return renderPdfDocToPng(doc, page, width);
            }
        }
    }

    private byte[] renderPdfDocToPng(PDDocument doc, int page, int width) throws IOException {
        int idx = Math.max(0, Math.min(page - 1, doc.getNumberOfPages() - 1));
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage img = renderer.renderImageWithDPI(idx, 150); // 150–200 DPI
        return toPngScaled(img, width);
    }

    /* ===== Image ===== */
    private BufferedImage readImage(String filePath) throws IOException {
        if (isRemote(filePath)) {
            return ImageIO.read(new URL(filePath));
        } else {
            return ImageIO.read(Path.of(filePath).toFile());
        }
    }

    private byte[] toPngScaled(BufferedImage img, int width) throws IOException {
        if (img == null) throw new IOException("Cannot read image");
        int newW = width;
        int newH = (int) ((double) img.getHeight() * newW / img.getWidth());

        Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(out, "png", baos);
        return baos.toByteArray();
    }

    /* ===== Utils ===== */
    private boolean isRemote(String filePath) {
        return filePath.startsWith("http://") || filePath.startsWith("https://");
    }

    private boolean isPdfPath(String lower) {
        try {
            if (isRemote(lower)) {
                String path = URI.create(lower).getPath();
                return path != null && path.endsWith(".pdf");
            }
            return lower.endsWith(".pdf");
        } catch (Exception e) {
            return lower.endsWith(".pdf");
        }
    }

    private boolean isImagePath(String lower) {
        try {
            if (isRemote(lower)) {
                String path = URI.create(lower).getPath();
                lower = (path == null ? lower : path.toLowerCase());
            }
        } catch (Exception ignored) {}
        return lower.endsWith(".png") || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".webp");
    }
}

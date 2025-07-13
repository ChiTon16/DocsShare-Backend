package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.DocumentDTO;
import com.tonz.tonzdocs.model.Document;

public class DocumentService {

    public static DocumentDTO toDTO(Document doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.setDocumentId(doc.getDocumentId());
        dto.setTitle(doc.getTitle());
        dto.setFilePath(doc.getFilePath());
        dto.setUploadTime(doc.getUploadTime());

        // User
        if (doc.getUser() != null) {
            dto.setUserId(doc.getUser().getUserId());
            dto.setUserName(doc.getUser().getName());
        }

        // Subject
        if (doc.getSubject() != null) {
            dto.setSubjectId(doc.getSubject().getSubjectId());
            dto.setSubjectName(doc.getSubject().getName());
        }

        return dto;
    }
}

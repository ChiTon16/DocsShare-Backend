package com.tonz.tonzdocs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentDTO {
    private Integer documentId;
    private String title;
    private String filePath;
    private LocalDateTime uploadTime;

    private Integer userId;
    private String userName;

    private Integer subjectId;
    private String subjectName;
}

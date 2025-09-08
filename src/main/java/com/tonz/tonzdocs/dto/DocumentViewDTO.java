package com.tonz.tonzdocs.dto;

import java.time.LocalDateTime;

/** Dùng khi mở tài liệu để xem: metadata + resume + info cho viewer */
public record DocumentViewDTO(
        Integer documentId,
        String  title,
        String  filePath,
        LocalDateTime uploadTime,

        Integer userId,
        String  userName,

        Integer subjectId,
        String  subjectName,

        Integer lastPage,
        Double  percent,
        LocalDateTime lastReadAt,

        Integer totalPages,
        String  viewerUrl
) {}

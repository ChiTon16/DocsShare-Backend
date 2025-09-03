package com.tonz.tonzdocs.dto;

import java.time.LocalDateTime;

public record ContinueCardDTO(
        Integer documentId,
        String title,
        String filePath,
        Integer lastPage,
        Double percent,
        LocalDateTime lastReadAt
) {}

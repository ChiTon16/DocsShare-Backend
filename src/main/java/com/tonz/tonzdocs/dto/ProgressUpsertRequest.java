package com.tonz.tonzdocs.dto;

import java.time.LocalDateTime;

public record ProgressUpsertRequest(
        Integer documentId,
        Integer lastPage,
        Double percent,
        Long sessionReadSeconds
) {}

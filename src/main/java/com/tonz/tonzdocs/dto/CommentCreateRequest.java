package com.tonz.tonzdocs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
        @NotNull Integer documentId,
        Integer parentId,            // null -> comment gốc; có giá trị -> reply
        @NotBlank String content
) {}

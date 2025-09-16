package com.tonz.tonzdocs.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Integer commentId,
        String content,
        LocalDateTime postedAt,
        Integer userId,
        String userName,
        Integer documentId,
        String documentTitle,
        Integer parentId,
        long replyCount            // số reply trực tiếp (để UI decide có load tiếp)
) {}

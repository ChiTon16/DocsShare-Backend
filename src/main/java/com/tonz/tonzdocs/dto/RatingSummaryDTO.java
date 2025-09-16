package com.tonz.tonzdocs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RatingSummaryDTO {
    private Long upvotes;
    private Long downvotes;
    private Integer percent;       // 0..100, null nếu tổng = 0
    private String myRating;       // "up" | "down" | null
}

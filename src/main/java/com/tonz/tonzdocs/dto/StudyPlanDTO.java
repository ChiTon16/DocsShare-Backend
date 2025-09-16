package com.tonz.tonzdocs.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyPlanDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer itemsCount;
    private LocalDateTime updatedAt;
}

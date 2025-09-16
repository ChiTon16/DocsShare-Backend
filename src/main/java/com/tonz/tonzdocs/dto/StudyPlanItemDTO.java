// com.tonz.tonzdocs.dto.StudyPlanItemDTO.java
package com.tonz.tonzdocs.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class StudyPlanItemDTO {
    private Integer id;
    private Integer documentId;
    private String title;
    private String subjectName;
    private String userName;
    private Integer sortIndex;
    private String note;
}
// dto/RecentSubjectDTO.java
package com.tonz.tonzdocs.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecentSubjectDTO {
    private Integer subjectId;
    private String subjectName;   // ví dụ "English", "Triết học Mác – Lê nin"
    private String subjectCode;   // ví dụ "POLI1304"
    private long totalDocsInSubject;     // tổng số tài liệu của môn (để render "6,876 documents")
    private long docsViewedByUser;       // số tài liệu user đã mở trong môn (tuỳ bạn có muốn hiển thị)
    private LocalDateTime lastViewedAt;  // để sort/hiển thị "gần đây"
    private boolean following;           // để hiện nút Follow/Unfollow
}

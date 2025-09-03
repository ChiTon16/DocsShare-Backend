package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recent_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","document_id"})
)
@Data @NoArgsConstructor @AllArgsConstructor
public class RecentView {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "document_id", nullable=false)
    private Document document;

    /** Lần cuối cùng đọc (để sort) */
    private LocalDateTime viewedAt;

    /** (mới) trang cuối cùng, nếu là PDF/doc chia trang */
    private Integer lastPage;

    /** (mới) % hoàn thành 0..100 */
    private Double percent;

    /** (mới) tổng thời gian đọc tích luỹ (tuỳ chọn) */
    private Long totalReadSeconds;

    /** (mới) lần đầu mở */
    private LocalDateTime firstOpenedAt;
}


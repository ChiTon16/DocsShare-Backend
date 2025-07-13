package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recent_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    private LocalDateTime viewedAt;
}

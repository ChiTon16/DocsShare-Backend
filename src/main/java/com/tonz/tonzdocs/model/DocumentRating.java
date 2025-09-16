package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_rating",
        uniqueConstraints = @UniqueConstraint(name="uq_doc_user", columnNames = {"user_id","document_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentRating {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false) @JoinColumn(name = "document_id")
    private Document document;

    /** 1 = up, -1 = down */
    @Column(nullable = false)
    private Integer value;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

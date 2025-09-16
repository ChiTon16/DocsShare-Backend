package com.tonz.tonzdocs.model;

import com.tonz.tonzdocs.model.Document;
import com.tonz.tonzdocs.model.StudyPlan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// StudyPlanItem.java
@Entity
@Table(name = "study_plan_item",
        uniqueConstraints = @UniqueConstraint(name = "uq_plan_doc",
                columnNames = {"plan_id", "document_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_spi_plan"))
    private StudyPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_spi_document"))
    private Document document;

    @Column(name = "sort_index", nullable = false)
    private Integer sortIndex = 0;

    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }
}

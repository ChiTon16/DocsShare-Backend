package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comment",
        indexes = {
                @Index(name = "idx_comment_document", columnList = "document_id"),
                @Index(name = "idx_comment_parent", columnList = "parent_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /** Reply tới comment cha (nếu là reply) */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /** Danh sách reply con; xoá parent sẽ xoá luôn toàn bộ cây con */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("postedAt ASC")
    private List<Comment> replies;
}

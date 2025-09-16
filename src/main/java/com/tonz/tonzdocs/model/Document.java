package com.tonz.tonzdocs.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "documentId")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentId;

    private String title;
    private String filePath;

    private LocalDateTime uploadTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToMany(mappedBy = "document")
    private List<Comment> comments;

    @OneToMany(mappedBy = "document")
    private List<DownloadLog> downloads;

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    @Column(name = "download_count", nullable = false)
    private long downloadCount = 0;

    @Column(nullable = false)
    private Long upvoteCount = 0L;

    @Column(nullable = false)
    private Long downvoteCount = 0L;

    // getters/setters
    public long getViewCount() { return viewCount; }
    public void setViewCount(long v) { this.viewCount = v; }

    public long getDownloadCount() { return downloadCount; }
    public void setDownloadCount(long d) { this.downloadCount = d; }

    public long getUpvoteCount() { return upvoteCount; }
    public void setUpvoteCount(long u) { this.upvoteCount = u; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime t) { this.uploadTime = t; }
}

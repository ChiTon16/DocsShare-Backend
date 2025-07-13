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
}

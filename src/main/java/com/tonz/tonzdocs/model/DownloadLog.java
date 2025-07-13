package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    private LocalDateTime downloadedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;
}

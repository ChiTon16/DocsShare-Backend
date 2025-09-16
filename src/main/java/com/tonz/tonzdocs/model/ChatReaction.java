package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "chat_reaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id","sender_email","emoji"})
)
public class ChatReaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "message_id", nullable = false)
    public Long messageId;

    @Column(name = "sender_email", nullable = false, length = 320)
    public String senderEmail;

    @Column(nullable = false, length = 32)
    public String emoji; // ví dụ "👍", "❤️"

    @Column(nullable = false)
    public Instant createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}

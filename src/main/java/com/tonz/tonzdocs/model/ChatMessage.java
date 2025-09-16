package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_message")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "room_id", nullable = false)
    public Long roomId;

    // ✅ NEW: map đúng cột sender_id đang NOT NULL trong DB
    @Column(name = "sender_id", nullable = false)
    public Integer senderId;

    @Column(nullable = false, length = 320)
    public String senderEmail;

    @Column(length = 32, nullable = false)
    public String type;

    @Lob public String content;

    @Column(name = "parent_id")
    public Long parentId;

    @Column(nullable = false)
    public Instant createdAt;

    @PrePersist public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }
}
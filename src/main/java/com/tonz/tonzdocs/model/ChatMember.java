// src/main/java/com/tonz/tonzdocs/model/ChatMember.java
package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "chat_member",
        indexes = {
                @Index(name = "idx_chat_member_room", columnList = "room_id"),
                @Index(name = "idx_chat_member_user", columnList = "user_email")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_member_room_user",
                columnNames = {"room_id", "user_email"}
        )
)
@Getter @Setter @NoArgsConstructor
public class ChatMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_member_room")
    )
    private ChatRoom room;

    @Column(name = "user_email", nullable = false, length = 200)
    private String userEmail;

    //  mapping này cho đúng với DB
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();
}

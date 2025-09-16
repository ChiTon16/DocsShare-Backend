// SubjectFollow.java
package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "subject_follow")
@Data @NoArgsConstructor @AllArgsConstructor
public class SubjectFollow {

    @EmbeddedId
    private SubjectFollowId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId")
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("subjectId")
    @JoinColumn(name = "subject_id")
    @ToString.Exclude
    private Subject subject;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}

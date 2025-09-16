// SubjectFollowId.java
package com.tonz.tonzdocs.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class SubjectFollowId implements Serializable {
    private Integer userId;
    private Integer subjectId;
}

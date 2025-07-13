package com.tonz.tonzdocs.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    private String name;
    private String description;

    @OneToMany(mappedBy = "role")
    @JsonBackReference // ✅ thêm dòng này
    private List<User> users;
}

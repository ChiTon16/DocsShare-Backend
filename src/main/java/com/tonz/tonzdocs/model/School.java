package com.tonz.tonzdocs.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "school")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "schoolId")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer schoolId;

    private String name;
    private String address;

    @OneToMany(mappedBy = "school")
    private List<User> users;
}

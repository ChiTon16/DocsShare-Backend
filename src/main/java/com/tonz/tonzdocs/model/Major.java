package com.tonz.tonzdocs.model;

import jakarta.persistence.*;

@Entity
@Table(name = "major")
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    private Integer id;

    private String name;

    private String code;

    // Getter/Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

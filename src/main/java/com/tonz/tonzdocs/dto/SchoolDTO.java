package com.tonz.tonzdocs.dto;

import jakarta.validation.constraints.NotBlank;

public class SchoolDTO {
    private Integer id;
    @NotBlank(message = "Tên trường không được để trống")
    private String name;
    private String address;

    // Constructors
    public SchoolDTO() {}

    public SchoolDTO(Integer id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    // Getters and Setters
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String code) {
        this.address = code;
    }
}
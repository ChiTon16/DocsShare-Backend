package com.tonz.tonzdocs.dto;

import jakarta.validation.constraints.NotBlank;

public class MajorDTO {
    private Integer id;
    @NotBlank(message = "Tên ngành không được để trống")
    private String name;
    @NotBlank(message = "Mã ngành không được để trống")
    private String code;

    // Constructors
    public MajorDTO() {}

    public MajorDTO(Integer id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
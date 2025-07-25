package com.tonz.tonzdocs.dto;

public class SubjectDTO {
    private Integer id;
    private String name;
    private String description;

    public SubjectDTO(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    // Setters (nếu cần)
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}
package com.tonz.tonzdocs.dto;

public class UserDTO {
    private Integer userId;
    private String name;
    private String email;
    private String password;
    private Integer schoolId; // Sử dụng ID
    private Integer majorId;  // Sử dụng ID
    private String role;
    private boolean active;

    private String avatarUrl;

    // Constructors
    public UserDTO() {}

    public UserDTO(Integer userId, String name, String email, String password, Integer schoolId, Integer majorId, String role, boolean active) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.schoolId = schoolId;
        this.majorId = majorId;
        this.role = role;
        this.active = active;
    }

    public UserDTO(Integer userId, String name, String email, String role, boolean active) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getSchoolId() { return schoolId; }
    public void setSchoolId(Integer schoolId) { this.schoolId = schoolId; }
    public Integer getMajorId() { return majorId; }
    public void setMajorId(Integer majorId) { this.majorId = majorId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
// src/main/java/com/tonz/tonzdocs/dto/SubjectDTO.java
package com.tonz.tonzdocs.dto;

public class SubjectDTO {
    private Integer subjectId;
    private String name;
    private String code; // có/không tuỳ FE

    public SubjectDTO() {}
    public SubjectDTO(Integer subjectId, String name, String code) {
        this.subjectId = subjectId;
        this.name = name;
        this.code = code;
    }

    public Integer getSubjectId() { return subjectId; }
    public String getName() { return name; }
    public String getCode() { return code; }

    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
}

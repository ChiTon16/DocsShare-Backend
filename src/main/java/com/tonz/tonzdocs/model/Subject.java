package com.tonz.tonzdocs.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "subject")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
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

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subjectId;

    private String name;
    private String code;

    @OneToMany(mappedBy = "subject")
    private List<Document> documents;
}

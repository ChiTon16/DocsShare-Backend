package com.tonz.tonzdocs.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CurrentUserDTO {
    private Long id;
    private String name;
    private String email;
    private String avatarUrl;
    private String role;
    private SchoolDTO school;
    private MajorDTO major;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SchoolDTO {
        private Long id;
        private String name;
        private String address;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MajorDTO {
        private Long id;
        private String name;
        private String code;
    }
}

package com.cdweb.bookstore.modules.author.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {
    private Long id;
    private String name;
    private String bio;
    private String avatarUrl;
}
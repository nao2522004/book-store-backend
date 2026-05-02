package com.cdweb.bookstore.modules.publisher.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherDTO {
    private Long id;
    private String name;
    private String description;
    private String website;
}
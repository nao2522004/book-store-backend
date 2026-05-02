package com.cdweb.bookstore.modules.category.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;

    // Chỉ cần ID của danh mục cha, không cần bê nguyên object Category vào đây
    private Long parentId;
}
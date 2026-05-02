package com.cdweb.bookstore.modules.product.dto;

import com.cdweb.bookstore.modules.product.model.Book;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String isbn;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private Integer pages;
    private String language;
    private Long categoryId;
    private Long publisherId;
    private Instant publishedDate;
    private Book.Status status;
    private List<Long> authorIds; // Danh sách ID của các tác giả
}
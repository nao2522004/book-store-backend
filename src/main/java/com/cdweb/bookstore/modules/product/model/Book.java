package com.cdweb.bookstore.modules.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true)
    private String isbn;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    private Integer pages;
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Column(name = "published_date")
    private Instant publishedDate;

    @Enumerated(EnumType.STRING)
    private Status status;   // ACTIVE, INACTIVE, OUT_OF_STOCK

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // --- relationships ---

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
    @Builder.Default
    private List<Author> authors = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<BookImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "book")
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    /**
     * Trả về ảnh bìa (is_cover = true), fallback ảnh đầu tiên
     */
    public String getCoverUrl() {
        return images.stream().filter(BookImage::isCover).map(BookImage::getImageUrl).findFirst().orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
    }

    /**
     * Giá hiển thị: ưu tiên discount_price
     */
    public BigDecimal getEffectivePrice() {
        return discountPrice != null ? discountPrice : price;
    }

    public enum Status {ACTIVE, INACTIVE, OUT_OF_STOCK}
}
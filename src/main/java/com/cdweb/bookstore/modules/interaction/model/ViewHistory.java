package com.cdweb.bookstore.modules.interaction.model;

import com.cdweb.bookstore.modules.product.model.Book;
import com.cdweb.bookstore.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "view_histories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "viewed_at")
    private Instant viewedAt;

    @PrePersist
    void prePersist() {
        this.viewedAt = Instant.now();
    }
}
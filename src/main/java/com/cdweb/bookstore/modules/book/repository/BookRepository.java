package com.cdweb.bookstore.modules.product.repository;

import com.cdweb.bookstore.modules.product.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findBySlug(String slug);
    boolean existsByIsbn(String isbn);
    boolean existsBySlug(String slug);
}
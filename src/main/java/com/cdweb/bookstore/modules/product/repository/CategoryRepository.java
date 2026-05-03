package com.cdweb.bookstore.modules.product.repository;

import com.cdweb.bookstore.modules.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
    Optional<Category> findBySlug(String slug);
}
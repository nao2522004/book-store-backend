package com.cdweb.bookstore.modules.product.repository;

import com.cdweb.bookstore.modules.product.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
    Optional<Category> findBySlug(String slug);
    @Query("SELECT c FROM Category c WHERE " +
            ":keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> searchCategories(@Param("keyword") String keyword, Pageable pageable);
}
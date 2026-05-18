package com.cdweb.bookstore.modules.product.repository;

import com.cdweb.bookstore.modules.product.model.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    // Bạn có thể thêm check trùng tên nếu cần:
    // boolean existsByName(String name);
    @Query("SELECT p FROM Publisher p WHERE " +
            ":keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Publisher> searchPublishers(@Param("keyword") String keyword, Pageable pageable);
}
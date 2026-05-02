package com.cdweb.bookstore.modules.publisher.repository;

import com.cdweb.bookstore.modules.product.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    // Bạn có thể thêm check trùng tên nếu cần:
    // boolean existsByName(String name);
}
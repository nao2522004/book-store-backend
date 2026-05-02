package com.cdweb.bookstore.modules.product.repository;

import com.cdweb.bookstore.modules.product.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findBySlug(String slug);

    boolean existsByIsbn(String isbn);

    boolean existsBySlug(String slug);

    /**
     * Trừ tồn kho ATOMIC: Chống Race Condition bằng cách gộp "Kiểm tra & Cập nhật"
     * vào 1 câu lệnh duy nhất dưới DB.
     */
    @Modifying
    @Query("UPDATE Book b SET b.stockQuantity = b.stockQuantity - :qty " +
            "WHERE b.id = :id AND b.stockQuantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    /**
     * Hoàn tồn kho nguyên tử: Đảm bảo tính nhất quán dữ liệu khi nhiều đơn hàng
     * cùng bị hủy/hoàn trả đồng thời.
     */
    @Modifying
    @Query("UPDATE Book b SET b.stockQuantity = b.stockQuantity + :qty WHERE b.id = :id")
    void increaseStock(@Param("id") Long id, @Param("qty") int qty);
}
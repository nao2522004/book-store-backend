package com.cdweb.bookstore.modules.order.repository;

import com.cdweb.bookstore.modules.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ─── User queries ─────────────────────────────────────────────────────────

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.book
            WHERE o.user.id = :userId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.book
            LEFT JOIN FETCH o.coupon
            WHERE o.id = :orderId
            """)
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    // ─── Admin queries (pagination) ───────────────────────────────────────────

    /**
     * Admin: lấy tất cả đơn hàng có phân trang.
     */
    @Query(value = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.book
            LEFT JOIN FETCH o.user
            ORDER BY o.createdAt DESC
            """, countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllWithItems(Pageable pageable);

    /**
     * Admin: lọc theo trạng thái + phân trang.
     */
    @Query(value = """
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.book
            LEFT JOIN FETCH o.user
            WHERE o.status = :status
            """, countQuery = "SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Page<Order> findAllByStatusWithItems(@Param("status") Order.OrderStatus status, Pageable pageable);
}
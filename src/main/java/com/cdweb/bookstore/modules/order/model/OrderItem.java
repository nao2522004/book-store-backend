package com.cdweb.bookstore.modules.order.model;

import com.cdweb.bookstore.modules.product.model.Book;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Snapshot tên sách lúc đặt hàng (phòng trường hợp sách đổi tên)
     */
    @Column(name = "book_title_snapshot")
    private String bookTitleSnapshot;

    /**
     * Snapshot ảnh bìa lúc đặt hàng
     */
    @Column(name = "book_cover_snapshot")
    private String bookCoverSnapshot;

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
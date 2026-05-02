package com.cdweb.bookstore.modules.order.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.OrderResponse;
import com.cdweb.bookstore.modules.order.model.Order;
import com.cdweb.bookstore.modules.order.model.OrderItem;
import com.cdweb.bookstore.modules.order.repository.OrderRepository;
import com.cdweb.bookstore.modules.product.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    // Dùng Set để O(1) lookup, dễ mở rộng sau này.
    private static final Set<Order.OrderStatus> CANCELLABLE_STATUSES = Set.of(
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED
    );

    private final OrderRepository orderRepository;
    private final BookRepository  bookRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdWithItems(userId)
                .stream()
                .map(OrderResponse::fromOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng #" + orderId + " không tồn tại"));
        // Security: user chỉ được xem đơn hàng của chính mình
        assertOrderBelongsToUser(order, userId);

        return OrderResponse.fromOrder(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng #" + orderId + " không tồn tại"));

        assertOrderBelongsToUser(order, userId);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new RuntimeException(
                    "Không thể hủy đơn hàng ở trạng thái " + order.getStatus() +
                    ". Chỉ hủy được khi đơn ở trạng thái: " + CANCELLABLE_STATUSES);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        for (OrderItem item : order.getItems()) {
            bookRepository.increaseStock(item.getBook().getId(), item.getQuantity());
        }

        return OrderResponse.fromOrder(orderRepository.save(order));
    }

    /**
     * Luồng hợp lệ:
     * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
     *                                            ↘ CANCELLED / RETURNED
     */
    @Transactional
    public OrderResponse updateStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng #" + orderId + " không tồn tại"));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        // Nếu admin hủy đơn đang xử lý → hoàn lại tồn kho
        if (newStatus == Order.OrderStatus.CANCELLED || newStatus == Order.OrderStatus.RETURNED) {
            for (OrderItem item : order.getItems()) {
                bookRepository.increaseStock(item.getBook().getId(), item.getQuantity());
            }
        }

        return OrderResponse.fromOrder(orderRepository.save(order));
    }

    private void assertOrderBelongsToUser(Order order, Long userId) {
        if (!order.getUser().getId().equals(userId)) {
            // Trả về 404 thay vì 403 để tránh lộ thông tin đơn hàng tồn tại
            throw new ResourceNotFoundException("Đơn hàng #" + order.getId() + " không tồn tại");
        }
    }

    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == Order.OrderStatus.CONFIRMED  || next == Order.OrderStatus.CANCELLED;
            case CONFIRMED  -> next == Order.OrderStatus.PROCESSING || next == Order.OrderStatus.CANCELLED;
            case PROCESSING -> next == Order.OrderStatus.SHIPPED    || next == Order.OrderStatus.CANCELLED;
            case SHIPPED    -> next == Order.OrderStatus.DELIVERED  || next == Order.OrderStatus.RETURNED;
            default         -> false; // DELIVERED, CANCELLED, RETURNED không được chuyển tiếp
        };

        if (!valid) {
            throw new RuntimeException(
                    "Chuyển trạng thái không hợp lệ: " + current + " → " + next);
        }
    }
}
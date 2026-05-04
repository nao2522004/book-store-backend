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

    private static final Set<Order.OrderStatus> CANCELLABLE_STATUSES = Set.of(
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED
    );

    private final OrderRepository orderRepository;
    private final BookRepository  bookRepository;

    // ─── User: xem đơn hàng của mình ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdWithItems(userId)
                .stream()
                .map(OrderResponse::fromOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order = loadWithItems(orderId);
        assertOrderBelongsToUser(order, userId);
        return OrderResponse.fromOrder(order);
    }

    // ─── User: tự hủy đơn ────────────────────────────────────────────────────

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = loadWithItems(orderId);
        assertOrderBelongsToUser(order, userId);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new RuntimeException(
                    "Không thể hủy đơn hàng ở trạng thái " + order.getStatus() +
                    ". Chỉ hủy được khi đơn ở trạng thái: " + CANCELLABLE_STATUSES);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        restoreStock(order.getItems());

        return OrderResponse.fromOrder(orderRepository.save(order));
    }

    // ─── Admin: cập nhật trạng thái đơn hàng ─────────────────────────────────

    /**
     * State machine:
     * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
     *                                            ↘ CANCELLED / RETURNED
     */
    @Transactional
    public OrderResponse updateStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = loadWithItems(orderId);
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        // Hoàn tồn kho nếu đơn bị hủy hoặc trả hàng
        if (newStatus == Order.OrderStatus.CANCELLED ||
            newStatus == Order.OrderStatus.RETURNED) {
            restoreStock(order.getItems());
        }

        return OrderResponse.fromOrder(orderRepository.save(order));
    }

    // ─── Admin: cập nhật trạng thái thanh toán ────────────────────────────────

    /**
     * Luồng hợp lệ:
     *   UNPAID → PAID      (xác nhận đã nhận tiền)
     *   PAID   → REFUNDED  (hoàn tiền khi hủy/trả hàng)
     * UNPAID → REFUNDED không hợp lệ (chưa thu tiền thì không có gì để hoàn).
     */
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, Order.PaymentStatus newPaymentStatus) {
        Order order = loadWithItems(orderId);

        validatePaymentTransition(order.getPaymentStatus(), newPaymentStatus);
        order.setPaymentStatus(newPaymentStatus);

        return OrderResponse.fromOrder(orderRepository.save(order));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Order loadWithItems(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Đơn hàng #" + orderId + " không tồn tại"));
    }

    private void assertOrderBelongsToUser(Order order, Long userId) {
        // Trả 404 thay vì 403 để không lộ thông tin đơn hàng tồn tại
        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Đơn hàng #" + order.getId() + " không tồn tại");
        }
    }

    private void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            bookRepository.increaseStock(item.getBook().getId(), item.getQuantity());
        }
    }

    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == Order.OrderStatus.CONFIRMED  || next == Order.OrderStatus.CANCELLED;
            case CONFIRMED  -> next == Order.OrderStatus.PROCESSING || next == Order.OrderStatus.CANCELLED;
            case PROCESSING -> next == Order.OrderStatus.SHIPPED    || next == Order.OrderStatus.CANCELLED;
            case SHIPPED    -> next == Order.OrderStatus.DELIVERED  || next == Order.OrderStatus.RETURNED;
            default         -> false; // DELIVERED, CANCELLED, RETURNED: trạng thái cuối
        };

        if (!valid) {
            throw new RuntimeException(
                    "Chuyển trạng thái không hợp lệ: " + current + " → " + next);
        }
    }

    private void validatePaymentTransition(Order.PaymentStatus current, Order.PaymentStatus next) {
        boolean valid = switch (current) {
            case UNPAID   -> next == Order.PaymentStatus.PAID;
            case PAID     -> next == Order.PaymentStatus.REFUNDED;
            case REFUNDED -> false; // trạng thái cuối
        };

        if (!valid) {
            throw new RuntimeException(
                    "Chuyển trạng thái thanh toán không hợp lệ: " + current + " → " + next);
        }
    }
}
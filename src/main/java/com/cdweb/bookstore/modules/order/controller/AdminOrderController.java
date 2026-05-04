package com.cdweb.bookstore.modules.order.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.common.PageResponse;
import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.OrderResponse;
import com.cdweb.bookstore.modules.order.dto.UpdatePaymentStatusRequest;
import com.cdweb.bookstore.modules.order.model.Order;
import com.cdweb.bookstore.modules.order.repository.OrderRepository;
import com.cdweb.bookstore.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin quản lý toàn bộ đơn hàng:
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
// @formatter:off
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final OrderService    orderService;

    /**
     * GET /admin/orders?status=PENDING&page=0&size=20
     * Lấy tất cả đơn hàng, tuỳ chọn lọc theo trạng thái, có phân trang.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<Order> page = (status != null)
                ? orderRepository.findAllByStatusWithItems(status, pageable)
                : orderRepository.findAllWithItems(pageable);

        return ApiResponse.ok(PageResponse.from(page.map(OrderResponse::fromOrder)));
    }

    /**
     * GET /admin/orders/{id}
     * Xem chi tiết bất kỳ đơn hàng (không bị giới hạn userId).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @PathVariable Long id) {

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Đơn hàng #" + id + " không tồn tại"));

        return ApiResponse.ok(OrderResponse.fromOrder(order));
    }

    /**
     * PATCH /admin/orders/{id}/status?status=CONFIRMED
     * Cập nhật trạng thái đơn hàng theo state machine:
     * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
     *                                            ↘ CANCELLED / RETURNED
     * Logic validate + hoàn tồn kho nằm trong OrderService.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {

        return ApiResponse.ok(
                orderService.updateStatus(id, status),
                "Cập nhật trạng thái đơn hàng thành công");
    }

    /**
     * PATCH /admin/orders/{id}/payment
     * Cập nhật trạng thái thanh toán:
     *   UNPAID → PAID      (xác nhận đã nhận tiền – BANKING/MOMO/ZALOPAY)
     *   PAID   → REFUNDED  (hoàn tiền khi đơn bị huỷ/trả hàng)
     */
    @PatchMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        return ApiResponse.ok(
                orderService.updatePaymentStatus(id, request.paymentStatus()),
                "Cập nhật trạng thái thanh toán thành công");
    }
}
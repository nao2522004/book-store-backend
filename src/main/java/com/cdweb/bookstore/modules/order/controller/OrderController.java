package com.cdweb.bookstore.modules.order.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.*;
import com.cdweb.bookstore.modules.order.model.Order;
import com.cdweb.bookstore.modules.order.service.CheckoutService;
import com.cdweb.bookstore.modules.order.service.CouponService;
import com.cdweb.bookstore.modules.order.service.OrderService;
import com.cdweb.bookstore.modules.user.model.User;
import com.cdweb.bookstore.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
// @formatter:off
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService    orderService;
    private final CouponService   couponService;
    private final UserRepository  userRepository;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        OrderResponse order = checkoutService.checkout(userId, request);
        return ApiResponse.created(order, "Đặt hàng thành công! Mã đơn: #" + order.id());
    }

    /**
     * GET /orders/coupon/preview?code=SUMMER10&subtotal=500000
     * Kiểm tra mã giảm giá và xem trước số tiền được giảm (không thay đổi DB).
     */
    @GetMapping("/coupon/preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> previewCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal subtotal,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        User user = loadUser(userId);
        CouponValidationResponse result = couponService.previewCoupon(code, subtotal, user);
        return ApiResponse.ok(result, result.isValid() ? "Mã hợp lệ" : result.errorMessage());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        return ApiResponse.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        return ApiResponse.ok(orderService.getOrderDetail(id, userId));
    }

    /**
     * User tự hủy đơn hàng (chỉ khi PENDING hoặc CONFIRMED).
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        return ApiResponse.ok(orderService.cancelOrder(id, userId), "Hủy đơn hàng thành công");
    }

    /**
     * Admin cập nhật trạng thái đơn hàng theo state machine.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {

        return ApiResponse.ok(orderService.updateStatus(id, status), "Cập nhật trạng thái thành công");
    }

    /**
     * Extract userId từ JWT claim.
     * Claim "userId" được set trong JwtService.buildAccessToken() là kiểu Long,
     * nhưng JSON deserialization trả về Number → cần ép kiểu an toàn.
     */
    private Long extractUserId(Jwt jwt) {
        Object raw = jwt.getClaim("userId");
        if (raw instanceof Number number) {
            return number.longValue();
        }
        throw new RuntimeException("Token không hợp lệ: thiếu claim userId");
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }
}
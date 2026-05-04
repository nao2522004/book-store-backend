package com.cdweb.bookstore.modules.order.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.CouponValidationResponse;
import com.cdweb.bookstore.modules.order.service.CouponService;
import com.cdweb.bookstore.modules.user.model.User;
import com.cdweb.bookstore.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * User endpoint: xem trước mã giảm giá trước khi checkout.
 * Admin CRUD coupon → AdminCouponController (/admin/coupons).
 */
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CouponUserController {

    private final CouponService  couponService;
    private final UserRepository userRepository;

    /**
     * GET /coupons/preview?code=SUMMER10&subtotal=500000
     */
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> previewCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal subtotal,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        CouponValidationResponse result = couponService.previewCoupon(code, subtotal, user);
        return ApiResponse.ok(result, result.isValid() ? "Mã hợp lệ" : result.errorMessage());
    }

    private Long extractUserId(Jwt jwt) {
        Object raw = jwt.getClaim("userId");
        if (raw instanceof Number n) return n.longValue();
        throw new RuntimeException("Token không hợp lệ: thiếu claim userId");
    }
}
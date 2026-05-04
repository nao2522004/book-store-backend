package com.cdweb.bookstore.modules.order.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.order.dto.CouponRequest;
import com.cdweb.bookstore.modules.order.dto.CouponResponse;
import com.cdweb.bookstore.modules.order.service.CouponAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin CRUD cho Coupon.
 * User xem trước mã → CouponUserController (/coupons/preview).
 */
@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminCouponController {

    private final CouponAdminService couponAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        return ApiResponse.ok(couponAdminService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Long id) {
        return ApiResponse.ok(couponAdminService.getCouponById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        return ApiResponse.created(couponAdminService.createCoupon(request), "Tạo coupon thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        return ApiResponse.ok(couponAdminService.updateCoupon(id, request), "Cập nhật coupon thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponAdminService.deleteCoupon(id);
        return ApiResponse.ok(null, "Xóa coupon thành công");
    }
}
package com.cdweb.bookstore.modules.order.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.order.dto.AddressRequest;
import com.cdweb.bookstore.modules.order.dto.AddressResponse;
import com.cdweb.bookstore.modules.order.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User tự quản lý địa chỉ giao hàng của mình.
 */
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
// @formatter:off
public class AddressController {

    private final AddressService addressService;

    /** GET /addresses – danh sách địa chỉ của tôi */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(addressService.getMyAddresses(extractUserId(jwt)));
    }

    /** POST /addresses – thêm địa chỉ mới */
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        AddressResponse response = addressService.addAddress(extractUserId(jwt), request);
        return ApiResponse.created(response, "Thêm địa chỉ thành công");
    }

    /** PUT /addresses/{id} – cập nhật địa chỉ */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        AddressResponse response = addressService.updateAddress(extractUserId(jwt), id, request);
        return ApiResponse.ok(response, "Cập nhật địa chỉ thành công");
    }

    /** PATCH /addresses/{id}/default – đặt làm địa chỉ mặc định */
    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        AddressResponse response = addressService.setDefault(extractUserId(jwt), id);
        return ApiResponse.ok(response, "Đã đặt làm địa chỉ mặc định");
    }

    /** DELETE /addresses/{id} – xóa địa chỉ (không xóa được default) */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        addressService.deleteAddress(extractUserId(jwt), id);
        return ApiResponse.ok(null, "Xóa địa chỉ thành công");
    }

    private Long extractUserId(Jwt jwt) {
        Object raw = jwt.getClaim("userId");
        if (raw instanceof Number n) return n.longValue();
        throw new RuntimeException("Token không hợp lệ: thiếu claim userId");
    }
}
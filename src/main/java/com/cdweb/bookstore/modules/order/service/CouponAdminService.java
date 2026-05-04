package com.cdweb.bookstore.modules.order.service;

import com.cdweb.bookstore.common.exception.ResourceAlreadyExistsException;
import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.CouponRequest;
import com.cdweb.bookstore.modules.order.dto.CouponResponse;
import com.cdweb.bookstore.modules.order.model.Coupon;
import com.cdweb.bookstore.modules.order.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponAdminService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(CouponResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        return CouponResponse.from(loadCoupon(id));
    }

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.findByCode(request.code().toUpperCase()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                    "Mã coupon '" + request.code() + "' đã tồn tại");
        }

        Coupon coupon = Coupon.builder()
                .code(request.code().toUpperCase())
                .type(request.type())
                .value(request.value())
                .minOrderAmount(request.minOrderAmount())
                .maxDiscountAmount(request.maxDiscountAmount())
                .usageLimit(request.usageLimit())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status())
                .build();

        return CouponResponse.from(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = loadCoupon(id);

        // Kiểm tra trùng code với coupon KHÁC
        String newCode = request.code().toUpperCase();
        if (!newCode.equals(coupon.getCode())) {
            couponRepository.findByCode(newCode).ifPresent(existing -> {
                throw new ResourceAlreadyExistsException(
                        "Mã coupon '" + newCode + "' đã được sử dụng bởi coupon khác");
            });
            coupon.setCode(newCode);
        }

        coupon.setType(request.type());
        coupon.setValue(request.value());
        coupon.setMinOrderAmount(request.minOrderAmount());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());
        coupon.setStatus(request.status());

        return CouponResponse.from(couponRepository.save(coupon));
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = loadCoupon(id);
        if (coupon.getUsedCount() > 0) {
            throw new RuntimeException(
                    "Không thể xóa coupon đã được sử dụng " + coupon.getUsedCount() + " lần. " +
                    "Hãy chuyển trạng thái sang INACTIVE nếu muốn vô hiệu hóa.");
        }
        couponRepository.delete(coupon);
    }

    private Coupon loadCoupon(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy coupon với ID: " + id));
    }
}
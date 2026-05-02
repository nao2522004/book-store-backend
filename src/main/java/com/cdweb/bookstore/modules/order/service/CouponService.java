package com.cdweb.bookstore.modules.order.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.CouponValidationResponse;
import com.cdweb.bookstore.modules.order.model.Coupon;
import com.cdweb.bookstore.modules.order.model.CouponUsage;
import com.cdweb.bookstore.modules.order.model.Order;
import com.cdweb.bookstore.modules.order.repository.CouponRepository;
import com.cdweb.bookstore.modules.order.repository.CouponUsageRepository;
import com.cdweb.bookstore.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    /**
     * Validate mã giảm giá và trả về kết quả để hiển thị cho user.
     * Không thực hiện bất kỳ thay đổi DB nào — chỉ đọc và tính toán.
     *
     * @param code        mã giảm giá
     * @param orderAmount tổng tiền hàng (chưa gồm ship)
     * @param user        user đang đăng nhập
     */
    @Transactional(readOnly = true)
    public CouponValidationResponse previewCoupon(String code, BigDecimal orderAmount, User user) {
        Coupon coupon = couponRepository.findByCode(code).orElse(null);

        if (coupon == null) {
            return CouponValidationResponse.invalid(code, "Mã giảm giá không tồn tại");
        }

        String invalidReason = findInvalidReason(coupon, user, orderAmount);
        if (invalidReason != null) {
            return CouponValidationResponse.invalid(code, invalidReason);
        }

        BigDecimal discount = coupon.calculateDiscount(orderAmount);
        return CouponValidationResponse.valid(code, coupon.getType().name(), discount);
    }

    /**
     * Kiểm tra tính hợp lệ của coupon ở thời điểm checkout.
     * Coupon phải được load bằng findByCodeForUpdate (đã lock) trước khi gọi hàm này.
     *
     * @throws RuntimeException nếu coupon không hợp lệ
     */
    public void assertCouponValid(Coupon coupon, User user, BigDecimal orderAmount) {
        String reason = findInvalidReason(coupon, user, orderAmount);
        if (reason != null) {
            throw new RuntimeException("Mã giảm giá '" + coupon.getCode() + "': " + reason);
        }
    }

    /**
     * Ghi nhận việc sử dụng coupon sau khi đơn hàng được lưu.
     * Tăng usedCount và tạo bản ghi CouponUsage.
     * <p>
     * Phải được gọi trong cùng @Transactional với checkout để rollback đồng bộ.
     */
    public void recordUsage(Coupon coupon, User user, Order order) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        CouponUsage usage = CouponUsage.builder().coupon(coupon).user(user).order(order).build();
        couponUsageRepository.save(usage);
    }

    /**
     * Trả về lý do không hợp lệ, hoặc null nếu coupon hợp lệ.
     */
    private String findInvalidReason(Coupon coupon, User user, BigDecimal orderAmount) {
        if (!coupon.isValid(orderAmount)) {
            if (coupon.getStatus() != Coupon.CouponStatus.ACTIVE) {
                return "Mã giảm giá không còn hoạt động";
            }
            if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
                return "Đơn hàng tối thiểu " + coupon.getMinOrderAmount() + "đ mới được dùng mã này";
            }
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                return "Mã giảm giá đã hết lượt sử dụng";
            }
            return "Mã giảm giá đã hết hạn hoặc chưa đến thời gian áp dụng";
        }

        if (couponUsageRepository.existsByCouponAndUser(coupon, user)) {
            return "Bạn đã sử dụng mã giảm giá này rồi";
        }

        return null; // hợp lệ
    }
}
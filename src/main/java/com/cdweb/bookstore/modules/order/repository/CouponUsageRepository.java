package com.cdweb.bookstore.modules.order.repository;

import com.cdweb.bookstore.modules.order.model.Coupon;
import com.cdweb.bookstore.modules.order.model.CouponUsage;
import com.cdweb.bookstore.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    /**
     * Kiểm tra user đã từng dùng coupon này chưa.
     */
    boolean existsByCouponAndUser(Coupon coupon, User user);
}
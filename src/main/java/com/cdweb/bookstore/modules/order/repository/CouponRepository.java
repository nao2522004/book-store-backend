package com.cdweb.bookstore.modules.order.repository;

import com.cdweb.bookstore.modules.order.model.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    /**
     * Tìm coupon theo code với PESSIMISTIC_WRITE lock (SELECT ... FOR UPDATE).
     * Dùng trong checkout để ngăn 2 request đồng thời cùng sử dụng 1 coupon
     * vượt quá usageLimit (race condition).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.code = :code")
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
}
package com.cdweb.bookstore.modules.order.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.CheckoutRequest;
import com.cdweb.bookstore.modules.order.dto.OrderResponse;
import com.cdweb.bookstore.modules.order.model.*;
import com.cdweb.bookstore.modules.order.repository.*;
import com.cdweb.bookstore.modules.product.model.Book;
import com.cdweb.bookstore.modules.product.repository.BookRepository;
import com.cdweb.bookstore.modules.user.model.User;
import com.cdweb.bookstore.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final BigDecimal SHIPPING_FEE          = new BigDecimal("30000");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("300000");

    private final UserRepository       userRepository;
    private final CartRepository       cartRepository;
    private final OrderRepository      orderRepository;
    private final AddressRepository    addressRepository;
    private final CouponRepository     couponRepository;
    private final BookRepository       bookRepository;
    private final CouponService        couponService;

    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống, không thể đặt hàng");
        }

        BigDecimal subtotal = validateStockAndCalcSubtotal(cart.getItems());

        Address address = addressRepository.findByIdAndUserId(request.addressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ giao hàng không tồn tại hoặc không thuộc về bạn"));

        BigDecimal shippingFee = calcShippingFee(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            appliedCoupon = couponRepository.findByCodeForUpdate(request.couponCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Mã giảm giá không tồn tại: " + request.couponCode()));

            couponService.assertCouponValid(appliedCoupon, user, subtotal);
            discountAmount = appliedCoupon.calculateDiscount(subtotal);
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee);

        Order order = Order.builder()
                .user(user)
                .coupon(appliedCoupon)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(Order.PaymentStatus.UNPAID)
                .recipientName(address.getFullName())
                .recipientPhone(address.getPhone())
                .shippingAddress(buildAddressSnapshot(address))
                .note(request.note())
                .build();

        // ── Tạo OrderItems với snapshot dữ liệu tại thời điểm đặt ─────────
        // Snapshot title/ảnh phòng trường hợp admin đổi thông tin sách sau này
        List<OrderItem> orderItems = buildOrderItems(cart.getItems(), order);
        order.getItems().addAll(orderItems);

        Order savedOrder = orderRepository.save(order);

        decreaseStockOrThrow(cart.getItems());

        if (appliedCoupon != null) {
            couponService.recordUsage(appliedCoupon, user, savedOrder);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponse.fromOrder(savedOrder);
    }

    /**
     * Kiểm tra từng sản phẩm trong giỏ:
     *  - Sách phải đang ACTIVE
     *  - Tồn kho phải đủ
     * Đồng thời tính subtotal để tránh loop thêm lần nữa.
     */
    private BigDecimal validateStockAndCalcSubtotal(List<CartItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            Book book = item.getBook();

            if (book.getStatus() != Book.Status.ACTIVE) {
                throw new RuntimeException(
                        "Sách \"" + book.getTitle() + "\" hiện không còn bán, vui lòng xóa khỏi giỏ hàng");
            }

            if (book.getStockQuantity() == null || book.getStockQuantity() < item.getQuantity()) {
                int available = book.getStockQuantity() != null ? book.getStockQuantity() : 0;
                throw new RuntimeException(
                        "Sách \"" + book.getTitle() + "\" chỉ còn " + available + " cuốn trong kho (bạn đang chọn " + item.getQuantity() + ")");
            }

            subtotal = subtotal.add(book.getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        return subtotal;
    }

    /**
     * Miễn phí ship nếu subtotal >= ngưỡng, ngược lại tính phí cố định.
     */
    private BigDecimal calcShippingFee(BigDecimal subtotal) {
        return subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_FEE;
    }

    /**
     * Build snapshot địa chỉ thành 1 chuỗi text.
     * Lưu dạng text để đơn hàng không bị ảnh hưởng khi user sau này chỉnh sửa địa chỉ.
     */
    private String buildAddressSnapshot(Address address) {
        List<String> parts = new ArrayList<>();
        if (address.getStreet()   != null) parts.add(address.getStreet());
        if (address.getWard()     != null) parts.add(address.getWard());
        if (address.getDistrict() != null) parts.add(address.getDistrict());
        if (address.getProvince() != null) parts.add(address.getProvince());
        return String.join(", ", parts);
    }

    /**
     * Chuyển CartItem → OrderItem với snapshot title và ảnh bìa.
     */
    private List<OrderItem> buildOrderItems(List<CartItem> cartItems, Order order) {
        return cartItems.stream().map(item -> {
            Book book = item.getBook();
            return OrderItem.builder()
                    .order(order)
                    .book(book)
                    .quantity(item.getQuantity())
                    .unitPrice(book.getEffectivePrice())
                    .bookTitleSnapshot(book.getTitle())
                    .bookCoverSnapshot(book.getCoverUrl())
                    .build();
        }).toList();
    }

    /**
     * Trừ tồn kho bằng atomic UPDATE.
     * Nếu trả về 0 dòng bị ảnh hưởng → tồn kho đã thay đổi giữa bước validate và bước này
     * (race condition) → ném exception để rollback toàn bộ transaction.
     */
    private void decreaseStockOrThrow(List<CartItem> items) {
        for (CartItem item : items) {
            int rowsAffected = bookRepository.decreaseStock(item.getBook().getId(), item.getQuantity());
            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Không thể trừ tồn kho cho sách \"" + item.getBook().getTitle() +
                        "\" — tồn kho đã thay đổi. Vui lòng kiểm tra lại giỏ hàng");
            }
        }
    }
}
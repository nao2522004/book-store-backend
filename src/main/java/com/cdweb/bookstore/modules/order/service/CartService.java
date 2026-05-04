package com.cdweb.bookstore.modules.order.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.AddToCartRequest;
import com.cdweb.bookstore.modules.order.dto.CartResponse;
import com.cdweb.bookstore.modules.order.dto.UpdateCartItemRequest;
import com.cdweb.bookstore.modules.order.model.Cart;
import com.cdweb.bookstore.modules.order.model.CartItem;
import com.cdweb.bookstore.modules.order.repository.CartRepository;
import com.cdweb.bookstore.modules.product.model.Book;
import com.cdweb.bookstore.modules.product.repository.BookRepository;
import com.cdweb.bookstore.modules.user.model.User;
import com.cdweb.bookstore.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository  cartRepository;
    private final BookRepository  bookRepository;
    private final UserRepository  userRepository;

    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Book book = loadActiveBook(request.bookId());

        // Nếu sách đã có trong giỏ → cộng dồn số lượng
        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getBook().getId().equals(book.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + request.quantity();
            assertSufficientStock(book, newQty);
            existingItem.setQuantity(newQty);
        } else {
            assertSufficientStock(book, request.quantity());
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(request.quantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long bookId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Book book = loadActiveBook(bookId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sách #" + bookId + " không có trong giỏ hàng"));

        assertSufficientStock(book, request.quantity());
        item.setQuantity(request.quantity());

        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long bookId) {
        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems()
                .removeIf(i -> i.getBook().getId().equals(bookId));

        if (!removed) {
            throw new ResourceNotFoundException("Sách #" + bookId + " không có trong giỏ hàng");
        }

        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Lấy hoặc tạo mới cart cho user (mỗi user chỉ có 1 cart).
     */
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    private Book loadActiveBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId));
        if (book.getStatus() != Book.Status.ACTIVE) {
            throw new RuntimeException("Sách \"" + book.getTitle() + "\" hiện không còn bán");
        }
        return book;
    }

    private void assertSufficientStock(Book book, int requiredQty) {
        int available = book.getStockQuantity() != null ? book.getStockQuantity() : 0;
        if (available < requiredQty) {
            throw new RuntimeException(
                    "Sách \"" + book.getTitle() + "\" chỉ còn " + available +
                    " cuốn trong kho (bạn yêu cầu " + requiredQty + ")");
        }
    }
}
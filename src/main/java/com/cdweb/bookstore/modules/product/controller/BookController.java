package com.cdweb.bookstore.modules.product.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.product.dto.BookDTO;
import com.cdweb.bookstore.modules.product.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public endpoints – không cần đăng nhập.
 * Mọi thao tác write (create/update/delete) chuyển sang AdminBookController.
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.ok(bookService.getAllBooks(keyword, page, size, sortBy, sortDir));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getAllWithoutPagination() {
        return ApiResponse.ok(bookService.getAllBooks());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@RequestBody BookDTO bookDTO) {
        return ApiResponse.created(bookService.createBook(bookDTO), "Tạo sách thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        return ApiResponse.ok(bookService.getBookById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable Long id,
            @RequestBody BookDTO bookDTO) {
        return ApiResponse.ok(bookService.updateBook(id, bookDTO), "Cập nhật sách thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.ok(null, "Xóa sách thành công");
    }
}
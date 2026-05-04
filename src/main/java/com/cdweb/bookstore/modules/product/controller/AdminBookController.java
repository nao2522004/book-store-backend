package com.cdweb.bookstore.modules.product.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.product.dto.BookDTO;
import com.cdweb.bookstore.modules.product.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints để quản lý sách.
 * User chỉ đọc → BookController (/books).
 */
@RestController
@RequestMapping("/admin/books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminBookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@RequestBody BookDTO dto) {
        return ApiResponse.created(bookService.createBook(dto), "Tạo sách thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable Long id,
            @RequestBody BookDTO dto) {
        return ApiResponse.ok(bookService.updateBook(id, dto), "Cập nhật sách thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.ok(null, "Xóa sách thành công");
    }
}
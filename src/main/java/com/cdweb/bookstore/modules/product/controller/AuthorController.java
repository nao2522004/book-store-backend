package com.cdweb.bookstore.modules.product.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.product.service.AuthorService;
import com.cdweb.bookstore.modules.product.dto.AuthorDTO;
import com.cdweb.bookstore.modules.product.model.Author;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorDTO>>> getAllAuthors() {
        return ApiResponse.ok(authorService.getAllAuthors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorDTO>> getAuthorById(@PathVariable Long id) {
        return ApiResponse.ok(authorService.getAuthorById(id));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<AuthorDTO>> createAuthor(@RequestBody AuthorDTO authorDTO) {
        return ApiResponse.created(authorService.createAuthor(authorDTO), "Tạo tác giả thành công");
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorDTO>> updateAuthor(
            @PathVariable Long id,
            @RequestBody AuthorDTO authorDTO) {
        return ApiResponse.ok(authorService.updateAuthor(id, authorDTO), "Cập nhật tác giả thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ApiResponse.ok(null, "Xóa tác giả thành công");
    }
}
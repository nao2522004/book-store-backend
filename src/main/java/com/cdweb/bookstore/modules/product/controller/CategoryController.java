package com.cdweb.bookstore.modules.product.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.product.dto.CategoryDTO;
import com.cdweb.bookstore.modules.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public endpoints – không cần đăng nhập.
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        return ApiResponse.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getCategoryById(id));
    }
}
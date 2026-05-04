package com.cdweb.bookstore.modules.product.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.product.dto.CategoryDTO;
import com.cdweb.bookstore.modules.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints để quản lý danh mục.
 * User chỉ đọc → CategoryController (/categories).
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@RequestBody CategoryDTO dto) {
        return ApiResponse.created(categoryService.createCategory(dto), "Tạo danh mục thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO dto) {
        return ApiResponse.ok(categoryService.updateCategory(id, dto), "Cập nhật danh mục thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok(null, "Xóa danh mục thành công");
    }
}
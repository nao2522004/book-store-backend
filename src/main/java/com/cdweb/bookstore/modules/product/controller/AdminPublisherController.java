package com.cdweb.bookstore.modules.product.controller;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.product.dto.PublisherDTO;
import com.cdweb.bookstore.modules.product.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints để quản lý nhà xuất bản.
 */
@RestController
@RequestMapping("/admin/publishers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminPublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PublisherDTO>>> getAll() {
        return ApiResponse.ok(publisherService.getAllPublishers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublisherDTO>> getById(@PathVariable Long id) {
        return ApiResponse.ok(publisherService.getPublisherById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PublisherDTO>> create(@RequestBody PublisherDTO dto) {
        return ApiResponse.created(publisherService.createPublisher(dto), "Tạo nhà xuất bản thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PublisherDTO>> update(
            @PathVariable Long id,
            @RequestBody PublisherDTO dto) {
        return ApiResponse.ok(publisherService.updatePublisher(id, dto), "Cập nhật nhà xuất bản thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        publisherService.deletePublisher(id);
        return ApiResponse.ok(null, "Xóa nhà xuất bản thành công");
    }
}
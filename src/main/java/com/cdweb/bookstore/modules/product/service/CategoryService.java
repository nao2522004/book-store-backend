package com.cdweb.bookstore.modules.product.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.product.dto.CategoryDTO;
import com.cdweb.bookstore.modules.product.repository.CategoryRepository;
import com.cdweb.bookstore.modules.product.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Slug danh mục đã tồn tại: " + dto.getSlug());
        }
        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại: " + dto.getName());
        }

        Category category = Category.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .build();

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với ID: " + dto.getParentId()));
            category.setParent(parent);
        }

        return toDTO(categoryRepository.save(category));
    }



    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // R - READ BY ID
    public CategoryDTO getCategoryById(Long id) {
        return toDTO(categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id)));

    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getSlug() != null && !dto.getSlug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(dto.getSlug())) {
                throw new RuntimeException("Slug '" + dto.getSlug() + "' đã được sử dụng cho một danh mục khác.");
            }
            category.setSlug(dto.getSlug());
        }
        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new RuntimeException("Một danh mục không thể là cha của chính nó.");
            }
            if (category.getParent() == null || !category.getParent().getId().equals(dto.getParentId())) {
                Category parent = categoryRepository.findById(dto.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với ID: " + dto.getParentId()));
                category.setParent(parent);
            }
        }

        return toDTO(categoryRepository.save(category));
    }

    // D - DELETE
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (!category.getChildren().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa danh mục này vì vẫn còn " + category.getChildren().size() + " danh mục con.");
        }
        if (!category.getBooks().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa danh mục này vì đang chứa " + category.getBooks().size() + " cuốn sách.");
        }

        categoryRepository.delete(category);
    }
    private CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}
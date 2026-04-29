package com.cdweb.bookstore.modules.category;

import com.cdweb.bookstore.modules.category.dto.CategoryDTO;
import com.cdweb.bookstore.modules.category.repository.CategoryRepository;
import com.cdweb.bookstore.modules.product.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // C - CREATE
    @Transactional
    public Category createCategory(CategoryDTO dto) {
        // 1. Kiểm tra trùng lặp
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

        // 2. Xử lý danh mục cha (nếu có)
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với ID: " + dto.getParentId()));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    // R - READ ALL
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // R - READ BY ID
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
    }

    // U - UPDATE
    @Transactional
    public Category updateCategory(Long id, CategoryDTO dto) {
        Category existingCategory = getCategoryById(id);

        // 1. Cập nhật các trường văn bản cơ bản
        if (dto.getName() != null) {
            // Có thể thêm check trùng tên ở đây nếu hệ thống yêu cầu tên danh mục phải unique
            existingCategory.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            existingCategory.setDescription(dto.getDescription());
        }

        // 2. Cập nhật Slug và kiểm tra trùng lặp
        if (dto.getSlug() != null && !dto.getSlug().equals(existingCategory.getSlug())) {
            if (categoryRepository.existsBySlug(dto.getSlug())) {
                throw new RuntimeException("Slug '" + dto.getSlug() + "' đã được sử dụng cho một danh mục khác.");
            }
            existingCategory.setSlug(dto.getSlug());
        }

        // 3. Cập nhật Danh mục cha (Parent)
        if (dto.getParentId() != null) {
            // Ngăn chặn việc set danh mục cha là chính nó (Circular Reference)
            if (dto.getParentId().equals(id)) {
                throw new RuntimeException("Một danh mục không thể là cha của chính nó.");
            }

            // Chỉ truy vấn DB nếu id cha thực sự thay đổi
            if (existingCategory.getParent() == null || !existingCategory.getParent().getId().equals(dto.getParentId())) {
                Category newParent = categoryRepository.findById(dto.getParentId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha với ID: " + dto.getParentId()));
                existingCategory.setParent(newParent);
            }
        }

        // Lưu ý: Với cách cập nhật if != null, nếu bạn muốn "gỡ bỏ" danh mục cha (đưa nó về Root),
        // bạn có thể quy ước từ Frontend gửi lên parentId = -1 hoặc 0, và thêm một đoạn logic nhỏ ở đây:
        // if (dto.getParentId() != null && dto.getParentId() <= 0) { existingCategory.setParent(null); }

        return categoryRepository.save(existingCategory);
    }

    // D - DELETE
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // Ràng buộc an toàn: Không cho xóa nếu đang có danh mục con
        if (!category.getChildren().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục này vì vẫn còn " + category.getChildren().size() + " danh mục con. Hãy xóa hoặc di chuyển danh mục con trước.");
        }

        // Ràng buộc an toàn: Không cho xóa nếu đang có sách thuộc danh mục này
        if (!category.getBooks().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục này vì đang chứa " + category.getBooks().size() + " cuốn sách. Hãy cập nhật sách sang danh mục khác trước.");
        }

        categoryRepository.delete(category);
    }
}
package com.cdweb.bookstore.modules.book;

import com.cdweb.bookstore.modules.author.repository.AuthorRepository;
import com.cdweb.bookstore.modules.book.dto.BookDTO;
import com.cdweb.bookstore.modules.category.repository.CategoryRepository;
import com.cdweb.bookstore.modules.product.model.*;
import com.cdweb.bookstore.modules.product.repository.BookRepository;
import com.cdweb.bookstore.modules.publisher.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;

    @Transactional
    public Book createBook(BookDTO dto) {
        // 1. Kiểm tra trùng lặp
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new RuntimeException("Mã ISBN đã tồn tại: " + dto.getIsbn());
        }
        if (bookRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Slug đã tồn tại: " + dto.getSlug());
        }

        // 2. Kiểm tra Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));

        // 3. Kiểm tra Publisher
        Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà xuất bản với ID: " + dto.getPublisherId()));

        // 4. Kiểm tra danh sách Authors
        List<Author> authors = authorRepository.findAllById(dto.getAuthorIds());
        if (authors.size() != dto.getAuthorIds().size()) {
            throw new RuntimeException("Một hoặc nhiều tác giả không hợp lệ.");
        }

        // 5. Map DTO to Entity và lưu
        Book book = Book.builder()
                .title(dto.getTitle())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .isbn(dto.getIsbn())
                .price(dto.getPrice())
                .discountPrice(dto.getDiscountPrice())
                .stockQuantity(dto.getStockQuantity())
                .pages(dto.getPages())
                .language(dto.getLanguage())
                .category(category)
                .publisher(publisher)
                .publishedDate(dto.getPublishedDate())
                .status(dto.getStatus() != null ? dto.getStatus() : Book.Status.ACTIVE)
                .authors(authors)
                .build();

        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + id));
    }

    @Transactional
    public Book updateBook(Long id, BookDTO dto) {
        Book existingBook = getBookById(id);
        mapDtoToObject(dto,existingBook);
        return bookRepository.save(existingBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Không thể xóa. Không tìm thấy sách với ID: " + id);
        }
        bookRepository.deleteById(id);
    }
    private void mapDtoToObject(BookDTO dto, Book existingBook) {

        // 1. Cập nhật các trường cơ bản
        if (dto.getTitle() != null) {
            existingBook.setTitle(dto.getTitle());
        }

        // Tôi đã đưa description về dạng if chuẩn cho đồng bộ và an toàn với các trường khác
        if (dto.getDescription() != null) {
            existingBook.setDescription(dto.getDescription());
        }

        if (dto.getPrice() != null) {
            existingBook.setPrice(dto.getPrice());
        }

        if (dto.getDiscountPrice() != null) {
            existingBook.setDiscountPrice(dto.getDiscountPrice());
        }

        if (dto.getStockQuantity() != null) {
            existingBook.setStockQuantity(dto.getStockQuantity());
        }

        if (dto.getPages() != null) {
            existingBook.setPages(dto.getPages());
        }

        if (dto.getLanguage() != null) {
            existingBook.setLanguage(dto.getLanguage());
        }

        if (dto.getPublishedDate() != null) {
            existingBook.setPublishedDate(dto.getPublishedDate());
        }

        if (dto.getStatus() != null) {
            existingBook.setStatus(dto.getStatus());
        }

        // 2. Cập nhật các trường Unique (Cần kiểm tra trùng lặp)
        if (dto.getIsbn() != null && !dto.getIsbn().equals(existingBook.getIsbn())) {
            if (bookRepository.existsByIsbn(dto.getIsbn())) {
                throw new RuntimeException("Mã ISBN mới đã tồn tại ở một cuốn sách khác.");
            }
            existingBook.setIsbn(dto.getIsbn());
        }

        if (dto.getSlug() != null && !dto.getSlug().equals(existingBook.getSlug())) {
            if (bookRepository.existsBySlug(dto.getSlug())) {
                throw new RuntimeException("Slug mới đã tồn tại ở một cuốn sách khác.");
            }
            existingBook.setSlug(dto.getSlug());
        }

        // 3. Cập nhật các mối quan hệ (Relationships)
        // - Category
        if (dto.getCategoryId() != null &&
                (existingBook.getCategory() == null || !existingBook.getCategory().getId().equals(dto.getCategoryId()))) {
            Category newCategory = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));
            existingBook.setCategory(newCategory);
        }

        // - Publisher
        if (dto.getPublisherId() != null &&
                (existingBook.getPublisher() == null || !existingBook.getPublisher().getId().equals(dto.getPublisherId()))) {
            Publisher newPublisher = publisherRepository.findById(dto.getPublisherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà xuất bản với ID: " + dto.getPublisherId()));
            existingBook.setPublisher(newPublisher);
        }

        // - Authors
        if (dto.getAuthorIds() != null && !dto.getAuthorIds().isEmpty()) {
            List<Author> newAuthors = authorRepository.findAllById(dto.getAuthorIds());
            if (newAuthors.size() != dto.getAuthorIds().size()) {
                throw new RuntimeException("Một hoặc nhiều tác giả không hợp lệ.");
            }
            existingBook.setAuthors(newAuthors);
        }
    }
}
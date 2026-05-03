package com.cdweb.bookstore.modules.product.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.product.repository.AuthorRepository;
import com.cdweb.bookstore.modules.product.dto.BookDTO;
import com.cdweb.bookstore.modules.product.model.*;
import com.cdweb.bookstore.modules.product.repository.BookRepository;
import com.cdweb.bookstore.modules.product.repository.CategoryRepository;
import com.cdweb.bookstore.modules.product.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;

    @Transactional
    public BookDTO createBook(BookDTO dto) {
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new RuntimeException("Mã ISBN đã tồn tại: " + dto.getIsbn());
        }
        if (bookRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Slug đã tồn tại: " + dto.getSlug());
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));

        Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản với ID: " + dto.getPublisherId()));

        List<Author> authors = authorRepository.findAllById(dto.getAuthorIds());
        if (authors.size() != dto.getAuthorIds().size()) {
            throw new RuntimeException("Một hoặc nhiều tác giả không hợp lệ.");
        }

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

        return toDTO(bookRepository.save(book));
    }

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        return toDTO(book);
    }

    @Transactional
    public BookDTO updateBook(Long id, BookDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        setDtoToEntity(dto, book);
        return toDTO(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không thể xóa. Không tìm thấy sách với ID: " + id);
        }
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với ID: " + id));
        book.setIsDeleted(true);
        bookRepository.save(book);
    }
    private void setDtoToEntity(BookDTO dto, Book book) {
        if (dto.getTitle() != null)         book.setTitle(dto.getTitle());
        if (dto.getDescription() != null)   book.setDescription(dto.getDescription());
        if (dto.getPrice() != null)         book.setPrice(dto.getPrice());
        if (dto.getDiscountPrice() != null) book.setDiscountPrice(dto.getDiscountPrice());
        if (dto.getStockQuantity() != null) book.setStockQuantity(dto.getStockQuantity());
        if (dto.getPages() != null)         book.setPages(dto.getPages());
        if (dto.getLanguage() != null)      book.setLanguage(dto.getLanguage());
        if (dto.getPublishedDate() != null) book.setPublishedDate(dto.getPublishedDate());
        if (dto.getStatus() != null)        book.setStatus(dto.getStatus());

        if (dto.getIsbn() != null && !dto.getIsbn().equals(book.getIsbn())) {
            if (bookRepository.existsByIsbn(dto.getIsbn())) {
                throw new RuntimeException("Mã ISBN mới đã tồn tại ở một cuốn sách khác.");
            }
            book.setIsbn(dto.getIsbn());
        }

        if (dto.getSlug() != null && !dto.getSlug().equals(book.getSlug())) {
            if (bookRepository.existsBySlug(dto.getSlug())) {
                throw new RuntimeException("Slug mới đã tồn tại ở một cuốn sách khác.");
            }
            book.setSlug(dto.getSlug());
        }

        if (dto.getCategoryId() != null &&
                (book.getCategory() == null || !book.getCategory().getId().equals(dto.getCategoryId()))) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));
            book.setCategory(category);
        }

        if (dto.getPublisherId() != null &&
                (book.getPublisher() == null || !book.getPublisher().getId().equals(dto.getPublisherId()))) {
            Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản với ID: " + dto.getPublisherId()));
            book.setPublisher(publisher);
        }

        if (dto.getAuthorIds() != null && !dto.getAuthorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(dto.getAuthorIds());
            if (authors.size() != dto.getAuthorIds().size()) {
                throw new RuntimeException("Một hoặc nhiều tác giả không hợp lệ.");
            }
            book.setAuthors(authors);
        }
    }
    private BookDTO toDTO(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .discountPrice(book.getDiscountPrice())
                .stockQuantity(book.getStockQuantity())
                .pages(book.getPages())
                .language(book.getLanguage())
                .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
                .publisherId(book.getPublisher() != null ? book.getPublisher().getId() : null)
                .publishedDate(book.getPublishedDate())
                .status(book.getStatus())
                .build();
    }
}
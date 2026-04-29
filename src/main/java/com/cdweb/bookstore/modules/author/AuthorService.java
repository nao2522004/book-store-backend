package com.cdweb.bookstore.modules.author;

import com.cdweb.bookstore.modules.author.dto.AuthorDTO;
import com.cdweb.bookstore.modules.product.model.Author;
import com.cdweb.bookstore.modules.author.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tác giả với ID: " + id));
    }

    @Transactional
    public Author createAuthor(AuthorDTO dto) {
        Author author = Author.builder()
                .name(dto.getName())
                .bio(dto.getBio())
                .avatarUrl(dto.getAvatarUrl())
                .build();
        return authorRepository.save(author);
    }

    @Transactional
    public Author updateAuthor(Long id, AuthorDTO dto) {
        Author existingAuthor = getAuthorById(id);

        // Gọi hàm map riêng để code ở đây gọn gàng
        mapDtoToObject(dto, existingAuthor);

        return authorRepository.save(existingAuthor);
    }

    @Transactional
    public void deleteAuthor(Long id) {
        Author author = getAuthorById(id);

        // Kiểm tra ràng buộc: Nếu tác giả đang có sách thì không cho xóa (tùy nghiệp vụ)
        if (!author.getBooks().isEmpty()) {
            throw new RuntimeException("Không thể xóa tác giả này vì đang có " + author.getBooks().size() + " cuốn sách liên quan.");
        }

        authorRepository.delete(author);
    }

    /**
     * Hàm helper để map dữ liệu từ DTO sang Object hiện có
     * Chỉ cập nhật những trường không null
     */
    private void mapDtoToObject(AuthorDTO dto, Author existingAuthor) {
        if (dto.getName() != null) {
            existingAuthor.setName(dto.getName());
        }

        if (dto.getBio() != null) {
            existingAuthor.setBio(dto.getBio());
        }

        if (dto.getAvatarUrl() != null) {
            existingAuthor.setAvatarUrl(dto.getAvatarUrl());
        }
    }
}
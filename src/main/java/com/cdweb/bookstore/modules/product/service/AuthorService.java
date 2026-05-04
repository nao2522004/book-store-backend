package com.cdweb.bookstore.modules.product.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.product.dto.AuthorDTO;
import com.cdweb.bookstore.modules.product.model.Author;
import com.cdweb.bookstore.modules.product.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public AuthorDTO getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả với ID: " + id));
        return toDTO(author);
    }

    @Transactional
    public AuthorDTO createAuthor(AuthorDTO dto) {
        Author author = Author.builder()
                .name(dto.getName())
                .bio(dto.getBio())
                .avatarUrl(dto.getAvatarUrl())
                .build();
        return toDTO(authorRepository.save(author));
    }
    @Transactional
    public AuthorDTO updateAuthor(Long id, AuthorDTO dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả với ID: " + id));
        setDtoToEntity(dto, author);
        return toDTO(authorRepository.save(author));
    }
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả với ID: " + id));

        if (!author.getBooks().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa tác giả này vì đang có " + author.getBooks().size() + " cuốn sách liên quan.");
        }

        authorRepository.delete(author);
    }
    /**
     * Hàm helper để map dữ liệu từ DTO sang Object hiện có
     * Chỉ cập nhật những trường không null
     */
    private void setDtoToEntity(AuthorDTO dto, Author author) {
        if (dto.getName() != null)     author.setName(dto.getName());
        if (dto.getBio() != null)      author.setBio(dto.getBio());
        if (dto.getAvatarUrl() != null) author.setAvatarUrl(dto.getAvatarUrl());
    }
    private AuthorDTO toDTO(Author author) {
        return AuthorDTO.builder()
                .id(author.getId())
                .name(author.getName())
                .bio(author.getBio())
                .avatarUrl(author.getAvatarUrl())
                .build();
    }
}
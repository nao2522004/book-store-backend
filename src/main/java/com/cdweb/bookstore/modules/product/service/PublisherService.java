package com.cdweb.bookstore.modules.product.service;

import com.cdweb.bookstore.modules.product.dto.PublisherDTO;
import com.cdweb.bookstore.modules.product.model.Publisher;
import com.cdweb.bookstore.modules.product.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll();
    }

    public Publisher getPublisherById(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà xuất bản với ID: " + id));
    }

    @Transactional
    public Publisher createPublisher(PublisherDTO dto) {
        Publisher publisher = Publisher.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .build();
        return publisherRepository.save(publisher);
    }

    @Transactional
    public Publisher updatePublisher(Long id, PublisherDTO dto) {
        Publisher existingPublisher = getPublisherById(id);

        // Gọi hàm helper để ánh xạ dữ liệu an toàn
        mapDtoToObject(dto, existingPublisher);

        return publisherRepository.save(existingPublisher);
    }

    @Transactional
    public void deletePublisher(Long id) {
        Publisher publisher = getPublisherById(id);

        // Kiểm tra ràng buộc dữ liệu: Không cho xóa nếu đang có sách của NXB này
        if (!publisher.getBooks().isEmpty()) {
            throw new RuntimeException("Không thể xóa NXB này vì đang liên kết với " + publisher.getBooks().size() + " cuốn sách.");
        }

        publisherRepository.delete(publisher);
    }

    /**
     * Hàm helper: Chỉ map những trường khác null từ DTO sang Object
     */
    private void mapDtoToObject(PublisherDTO dto, Publisher existingPublisher) {
        if (dto.getName() != null) {
            existingPublisher.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            existingPublisher.setDescription(dto.getDescription());
        }

        if (dto.getWebsite() != null) {
            existingPublisher.setWebsite(dto.getWebsite());
        }
    }
}
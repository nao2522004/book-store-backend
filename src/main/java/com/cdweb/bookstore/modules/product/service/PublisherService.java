package com.cdweb.bookstore.modules.product.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
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

    public List<PublisherDTO> getAllPublishers() {
        return publisherRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public PublisherDTO getPublisherById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản với ID: " + id));
        return toDTO(publisher);
    }

    @Transactional
    public PublisherDTO createPublisher(PublisherDTO dto) {
        Publisher publisher = Publisher.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .build();
        return toDTO(publisherRepository.save(publisher));
    }

    @Transactional
    public PublisherDTO updatePublisher(Long id, PublisherDTO dto) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản với ID: " + id));
        setDtoToEntity(dto, publisher);
        return toDTO(publisherRepository.save(publisher));
    }

    @Transactional
    public void deletePublisher(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản với ID: " + id));
        if (!publisher.getBooks().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa NXB này vì đang liên kết với " + publisher.getBooks().size() + " cuốn sách.");
        }
        publisherRepository.delete(publisher);
    }

    /**
     * Hàm helper: Chỉ map những trường khác null từ DTO sang Object
     */
    private PublisherDTO toDTO(Publisher publisher) {
        return PublisherDTO.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .description(publisher.getDescription())
                .website(publisher.getWebsite())
                .build();
    }

    private void setDtoToEntity(PublisherDTO dto, Publisher publisher) {
        if (dto.getName() != null)        publisher.setName(dto.getName());
        if (dto.getDescription() != null) publisher.setDescription(dto.getDescription());
        if (dto.getWebsite() != null)     publisher.setWebsite(dto.getWebsite());
    }
}
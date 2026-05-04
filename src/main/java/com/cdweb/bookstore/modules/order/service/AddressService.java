package com.cdweb.bookstore.modules.order.service;

import com.cdweb.bookstore.common.exception.ResourceNotFoundException;
import com.cdweb.bookstore.modules.order.dto.AddressRequest;
import com.cdweb.bookstore.modules.order.dto.AddressResponse;
import com.cdweb.bookstore.modules.order.model.Address;
import com.cdweb.bookstore.modules.order.repository.AddressRepository;
import com.cdweb.bookstore.modules.user.model.User;
import com.cdweb.bookstore.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Nếu đây là địa chỉ default → bỏ default của địa chỉ cũ
        if (request.isDefault()) {
            clearCurrentDefault(userId);
        }

        // Nếu chưa có địa chỉ nào → tự động đặt làm default
        boolean hasNoAddress = addressRepository.findByUserId(userId).isEmpty();

        Address address = Address.builder().user(user).fullName(request.fullName()).phone(request.phone()).street(request.street()).ward(request.ward()).district(request.district()).province(request.province()).isDefault(request.isDefault() || hasNoAddress).build();

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = loadOwnedAddress(userId, addressId);

        if (request.isDefault() && !address.isDefault()) {
            clearCurrentDefault(userId);
        }

        address.setFullName(request.fullName());
        address.setPhone(request.phone());
        address.setStreet(request.street());
        address.setWard(request.ward());
        address.setDistrict(request.district());
        address.setProvince(request.province());
        address.setDefault(request.isDefault());

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {
        Address address = loadOwnedAddress(userId, addressId);

        if (!address.isDefault()) {
            clearCurrentDefault(userId);
            address.setDefault(true);
            addressRepository.save(address);
        }

        return AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = loadOwnedAddress(userId, addressId);

        if (address.isDefault()) {
            throw new RuntimeException("Không thể xóa địa chỉ mặc định. Hãy đặt địa chỉ khác làm mặc định trước.");
        }

        addressRepository.delete(address);
    }

    private Address loadOwnedAddress(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId).orElseThrow(() -> new ResourceNotFoundException("Địa chỉ #" + addressId + " không tồn tại hoặc không thuộc về bạn"));
    }

    private void clearCurrentDefault(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(old -> {
            old.setDefault(false);
            addressRepository.save(old);
        });
    }
}
package com.cdweb.bookstore.modules.order.dto;

import com.cdweb.bookstore.modules.order.model.Address;

public record AddressResponse(
        Long id,
        String fullName,
        String phone,
        String street,
        String ward,
        String district,
        String province,
        String fullAddress,   // chuỗi hiển thị đầy đủ
        boolean isDefault
) {
    public static AddressResponse from(Address address) {
        String full = String.join(", ",
                address.getStreet(),
                address.getWard(),
                address.getDistrict(),
                address.getProvince());

        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getStreet(),
                address.getWard(),
                address.getDistrict(),
                address.getProvince(),
                full,
                address.isDefault()
        );
    }
}
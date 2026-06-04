package com.cdweb.bookstore.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequest(

        @NotBlank(message = "Họ tên không được để trống")
        String fullName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{7,8})$",
                message = "Số điện thoại không hợp lệ (9-10 số, đầu số 03/05/07/08/09)")
        String phone,

        @NotBlank(message = "Số nhà, tên đường không được để trống")
        String street,

        @NotBlank(message = "Phường/Xã không được để trống")
        String ward,

        @NotBlank(message = "Quận/Huyện không được để trống")
        String district,

        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        String province,

        // true = đặt làm địa chỉ mặc định
        boolean isDefault
) {}
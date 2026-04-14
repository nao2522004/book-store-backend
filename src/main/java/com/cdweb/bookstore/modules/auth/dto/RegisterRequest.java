package com.cdweb.bookstore.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// @formatter:off
public record RegisterRequest(
        @NotBlank(message = "Tên người dùng không được để trống")
        String name,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Định dạng email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 20, message = "Mật khẩu phải từ 6 đến 20 ký tự")
        String password
) {
}
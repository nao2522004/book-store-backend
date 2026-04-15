package com.cdweb.bookstore.modules.auth.dto;

import com.cdweb.bookstore.modules.user.model.Role;
import com.cdweb.bookstore.modules.user.model.User;

import java.util.Set;

public record RegisterResponse(
        Long id,
        String name,
        String email,
        Set<String> roles
) {
    public static RegisterResponse fromUser(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(java.util.stream.Collectors.toSet())
        );
    }
}
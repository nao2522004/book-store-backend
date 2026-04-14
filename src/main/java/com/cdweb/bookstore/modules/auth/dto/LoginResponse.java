package com.cdweb.bookstore.modules.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,       // "Bearer"
        long expiresIn,         // seconds
        Long userId,
        String name,
        String email
) {}
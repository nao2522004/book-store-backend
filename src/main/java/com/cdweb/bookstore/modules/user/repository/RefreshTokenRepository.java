package com.cdweb.bookstore.modules.user.repository;

import com.cdweb.bookstore.modules.user.model.RefreshToken;
import com.cdweb.bookstore.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
}
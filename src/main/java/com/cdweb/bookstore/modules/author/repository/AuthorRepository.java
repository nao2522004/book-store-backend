package com.cdweb.bookstore.modules.author.repository;

import com.cdweb.bookstore.modules.product.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
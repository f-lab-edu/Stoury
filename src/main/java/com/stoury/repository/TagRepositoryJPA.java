package com.stoury.repository;

import com.stoury.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepositoryJPA extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);
}
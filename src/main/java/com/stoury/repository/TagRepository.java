package com.stoury.repository;

import com.stoury.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByTagName(String tagName);

    Optional<Tag> findByTagName(String tagName);
}
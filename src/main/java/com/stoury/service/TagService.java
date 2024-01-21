package com.stoury.service;

import com.stoury.domain.Tag;
import com.stoury.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional
    public Tag getTagOrElseCreate(String tagName) {
        return tagRepository
                .findByTagName(tagName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName)));
    }
}

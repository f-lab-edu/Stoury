package com.stoury.service;

import com.stoury.domain.Tag;
import com.stoury.exception.tag.TagCreateException;
import com.stoury.repository.TagRepository;
import com.stoury.utils.StopWords;
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
                .orElseGet(() -> tagRepository.save(createTagEntity(tagName)));
    }

    private Tag createTagEntity(String tagName) {
        if (tagName.length() < 3) {
            throw new TagCreateException("Tag length is too short. Length must be longer than 2 characters.");
        }
        if (StopWords.isStopWord(tagName)) {
            throw new TagCreateException("%s cannot be used as tag.".formatted(tagName));
        }

        return new Tag(tagName);
    }
}

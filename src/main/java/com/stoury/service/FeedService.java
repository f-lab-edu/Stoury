package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FileService fileService;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FeedResponse createFeed(Member writer, FeedCreateRequest feedCreateRequest,
                                   List<MultipartFile> graphicContents) {
        List<GraphicContent> savedContents = null;
        try {
            validate(graphicContents);
            validate(writer);

            savedContents = graphicContents.stream()
                    .map(file -> new GraphicContent(fileService.saveFile(file)))
                    .toList();

            Feed feed = feedCreateRequest.toEntity(writer, savedContents);

            Feed uploadedFeed = feedRepository.save(feed);

            return FeedResponse.from(uploadedFeed);
        } catch (Exception e) {
            manualRollback(savedContents);
            throw new FeedCreateException(e);
        }
    }

    private void validate(Member writer) {
        if (!memberRepository.existsById(writer.getId())) {
            throw new NoSuchElementException("Cannot find the member.");
        }
    }

    private void validate(List<MultipartFile> graphicContents) {
        if (Objects.requireNonNull(graphicContents).isEmpty()) {
            throw new IllegalArgumentException("You must upload with images or videos.");
        }
    }

    private void manualRollback(List<GraphicContent> savedContents) {
        if (hasAnyContents(savedContents)) {
            fileService.removeFiles(savedContents.stream().map(GraphicContent::getPath).toList());
        }
    }

    private boolean hasAnyContents(List<GraphicContent> savedContents) {
        return savedContents!= null && !savedContents.isEmpty();
    }
}

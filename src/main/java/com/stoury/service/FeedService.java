package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.event.FileSaveEvent;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedResponse createFeed(Member writer, FeedCreateRequest feedCreateRequest,
                                   List<MultipartFile> graphicContents) {
        validate(graphicContents);
        validate(writer);

        Map<MultipartFile, String> reservedImagePaths = graphicContents.stream()
                .collect(Collectors.toMap(Function.identity(), file -> UUID.randomUUID().toString()));

        List<GraphicContent> reservedContents = reservedImagePaths.values().stream()
                .map(GraphicContent::new)
                .toList();

        Feed feed = feedCreateRequest.toEntity(writer, reservedContents);

        Feed uploadedFeed = feedRepository.save(feed);

        eventPublisher.publishEvent(new FileSaveEvent(this, reservedImagePaths));

        return FeedResponse.from(uploadedFeed);
    }

    private void validate(Member writer) {
        if (!memberRepository.existsById(writer.getId())) {
            throw new FeedCreateException("Cannot find the member.");
        }
    }

    private void validate(List<MultipartFile> graphicContents) {
        if (Objects.requireNonNull(graphicContents).isEmpty()) {
            throw new FeedCreateException("You must upload with images or videos.");
        }
    }
}

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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedResponse createFeed(Member writer, FeedCreateRequest feedCreateRequest,
                                   List<MultipartFile> graphicContents) {
        validate(writer, feedCreateRequest, graphicContents);

        Feed feedEntity = createFeedEntity(writer, feedCreateRequest, graphicContents);

        Feed uploadedFeed = feedRepository.save(feedEntity);

        requestToSaveFile(graphicContents, uploadedFeed);

        return FeedResponse.from(uploadedFeed);
    }

    private void requestToSaveFile(List<MultipartFile> graphicContents, Feed uploadedFeed) {
        uploadedFeed.getGraphicContents().forEach(graphicContent -> {
            MultipartFile fileToSave = graphicContents.get(graphicContent.getSequence());
            String path = graphicContent.getPath();
            Pair<MultipartFile, String> fileToSaveWithPath = Pair.of(fileToSave, path);
            FileSaveEvent fileSaveEvent = new FileSaveEvent(this, fileToSaveWithPath);
            eventPublisher.publishEvent(fileSaveEvent);
        });
    }

    private Feed createFeedEntity(Member writer, FeedCreateRequest feedCreateRequest, List<MultipartFile> graphicContents) {
        List<GraphicContent> reservedContents = IntStream.range(0, graphicContents.size())
                .mapToObj(GraphicContent::createTemporalGraphicContent)
                .toList();

        return feedCreateRequest.toEntity(writer, reservedContents);
    }

    private void validate(Member writer, FeedCreateRequest feedCreateRequest, List<MultipartFile> graphicContents) {
        if (!memberRepository.existsById(writer.getId())) {
            throw new FeedCreateException("Cannot find the member.");
        }
        if (Objects.requireNonNull(graphicContents).isEmpty()) {
            throw new FeedCreateException("You must upload with images or videos.");
        }
        if (feedCreateRequest.longitude() == null || feedCreateRequest.latitude() == null) {
            throw new FeedCreateException("Location information is required.");
        }
    }
}

package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.GraphicContent;
import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.dto.FeedResponse;
import com.stoury.event.GraphicSaveEvent;
import com.stoury.exception.FeedCreateException;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.FileUtils;
import com.stoury.utils.SupportedFileType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FeedService {
    public static final String PATH_PREFIX = "/feeds";
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FeedResponse createFeed(Member writer, FeedCreateRequest feedCreateRequest,
                                   List<MultipartFile> graphicContentsFiles) {
        validate(writer, feedCreateRequest, graphicContentsFiles);

        List<GraphicContent> graphicContents = requestToSaveFile(graphicContentsFiles);

        Feed feedEntity = createFeedEntity(writer, feedCreateRequest, graphicContents);

        Feed uploadedFeed = feedRepository.save(feedEntity);

        return FeedResponse.from(uploadedFeed);
    }

    private List<GraphicContent> requestToSaveFile(List<MultipartFile> graphicContents) {
        return IntStream.range(0, graphicContents.size())
                .mapToObj(seq -> {
                    MultipartFile fileToSave = graphicContents.get(seq);
                    GraphicContent graphicContent = createGraphicContent(seq, fileToSave);
                    Pair<MultipartFile, String> fileToSaveWithPath = Pair.of(fileToSave, graphicContent.getPath());
                    GraphicSaveEvent graphicSaveEvent = new GraphicSaveEvent(this, fileToSaveWithPath);
                    eventPublisher.publishEvent(graphicSaveEvent);
                    return graphicContent;
                })
                .toList();
    }

    private Feed createFeedEntity(Member writer, FeedCreateRequest feedCreateRequest, List<GraphicContent> graphicContents) {
        return feedCreateRequest.toEntity(writer, graphicContents);
    }

    private GraphicContent createGraphicContent(int seq, MultipartFile file) {
        SupportedFileType fileType = SupportedFileType.getFileType(file);

        if (SupportedFileType.OTHER.equals(fileType)) {
            throw new FeedCreateException("The File format is not supported.");
        }

        String path = PATH_PREFIX + fileType.getPath() + "/" + FileUtils.getFileNameByCurrentTime() + fileType.getExtension();

        return new GraphicContent(path, seq);
    }

    private void validate(Member writer, FeedCreateRequest feedCreateRequest, List<MultipartFile> graphicContents) {
        if (!memberRepository.existsById(writer.getId())) {
            throw new FeedCreateException("Cannot find the member.");
        }
        if (graphicContents.isEmpty()) {
            throw new FeedCreateException("You must upload with images or videos.");
        }
        if (feedCreateRequest.longitude() == null || feedCreateRequest.latitude() == null) {
            throw new FeedCreateException("Location information is required.");
        }
    }
}

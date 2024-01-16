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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {
    private final FileService fileService;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;

    public FeedResponse createFeed(FeedCreateRequest feedCreateRequest, List<MultipartFile> graphicContents) {
        Member writer = memberRepository.findById(Objects.requireNonNull(feedCreateRequest.writerId()))
                .orElseThrow(() -> new FeedCreateException("Cannot find the member."));
        if (graphicContents.isEmpty()) {
            throw new FeedCreateException("You must upload with images or videos.");
        }

        List<GraphicContent> savedContents = graphicContents.stream()
                .map(file -> new GraphicContent(fileService.saveFile(file)))
                .toList();

        Feed feed = feedCreateRequest.toEntity(writer, savedContents);

        Feed uploadedFeed = feedRepository.save(feed);

        return FeedResponse.from(uploadedFeed);
    }

}

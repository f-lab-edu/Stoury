package com.stoury.domain;

import com.stoury.dto.feed.FeedUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FEED",
        indexes = @Index(name = "INDEX_CREATED_AT", columnList = "CREATED_AT")
)
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @Column(name = "CREATED_AT")
    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GraphicContent> graphicContents = new ArrayList<>();

    @Column(name = "TEXT_CONTENT", nullable = false, columnDefinition = "text")
    private String textContent;

    @Column(name = "LATITUDE", nullable = false)
    private Double latitude;

    @Column(name = "LONGITUDE", nullable = false)
    private Double longitude;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(joinColumns = @JoinColumn(name = "FEED_ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
    private List<Tag> tags = new ArrayList<>();

    @Builder
    public Feed(Member member, String textContent, Double latitude, Double longitude, List<Tag> tags) {
        this.member = member;
        this.textContent = textContent;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tags = tags;
    }

    public void addGraphicContent(GraphicContent graphicContent) {
        graphicContents.add(graphicContent);
    }

    public void addGraphicContents(List<GraphicContent> graphicContentsPaths) {
        graphicContentsPaths.forEach(this::addGraphicContent);
    }

    public void update(FeedUpdateRequest feedUpdateRequest, List<Tag> tags) {
        this.textContent = feedUpdateRequest.textContent();
        this.longitude = feedUpdateRequest.longitude();
        this.latitude = feedUpdateRequest.latitude();
        this.tags = tags;

        List<GraphicContent> toDeleteGraphics = new ArrayList<>();
        for (Integer sequence : feedUpdateRequest.deleteGraphicContentSequence()) {
            GraphicContent toDeleteGraphic = graphicContents.stream()
                    .filter(graphicContent -> graphicContent.getSequence() == sequence)
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
            toDeleteGraphics.add(toDeleteGraphic);
        }

        graphicContents.removeAll(toDeleteGraphics);

        /*
          0, 1, 2, 3, 4 에서 1번 3번이 지워지면
          0, -, 2, -, 4가 됨. 번호를 앞으로 당기는 작업 수행
          0, 1, 2
         */
        for (int i = 0; i < graphicContents.size(); i++) {
            graphicContents.get(i).setSequence(i);
        }
    }
}

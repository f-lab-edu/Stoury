package com.stoury.domain;

import com.stoury.dto.feed.FeedUpdateRequest;
import com.stoury.utils.cachekeys.PageSize;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FEED")
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "CREATED_AT")
    @CreatedDate
    private LocalDateTime createdAt;

    @BatchSize(size = PageSize.FEED_PAGE_SIZE)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "feed")
    private List<GraphicContent> graphicContents = new ArrayList<>();

    @Column(name = "TEXT_CONTENT", nullable = false, columnDefinition = "text")
    private String textContent;

    @Column(name = "LATITUDE", nullable = false)
    private Double latitude;

    @Column(name = "LONGITUDE", nullable = false)
    private Double longitude;

    @BatchSize(size = PageSize.FEED_PAGE_SIZE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "FEED_TAG",
            joinColumns = @JoinColumn(name = "FEED_ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "CITY", length = 35, nullable = false)
    private String city = "UNDEFINED";

    @Column(name = "COUNTRY", length = 50, nullable = false)
    private String country = "UNDEFINED";

    @Builder
    public Feed(Member member, String textContent, Double latitude, Double longitude,
                Set<Tag> tags, String city, String country) {
        this.member = member;
        this.textContent = textContent;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tags = tags;
        this.city = city;
        this.country = country;
    }

    public void addGraphicContent(GraphicContent graphicContent) {
        graphicContents.add(graphicContent);
        graphicContent.setFeed(this);
    }

    public void addGraphicContents(List<GraphicContent> graphicContentsPaths) {
        graphicContentsPaths.forEach(this::addGraphicContent);
    }

    public void update(FeedUpdateRequest feedUpdateRequest) {
        this.textContent = feedUpdateRequest.textContent();
    }

    public void updateTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void deleteSelectedGraphics(Set<Integer> deleteGraphicContentSequences) {
        List<GraphicContent> toDeleteGraphics = new ArrayList<>();

        for (GraphicContent graphicContent : graphicContents) {
            if (deleteGraphicContentSequences.contains(graphicContent.getSequence())) {
                toDeleteGraphics.add(graphicContent);
            }
        }

        graphicContents.removeAll(toDeleteGraphics);
        /*
          0, 1, 2, 3, 4 에서 1번 3번이 지워지면
          0, -, 2, -, 4가 됨. 번호를 앞으로 당기는 작업 수행
          0, 2, 4
         */
        for (int i = 0; i < graphicContents.size(); i++) {
            graphicContents.get(i).setSequence(i);
        }
    }

    public boolean isWrittenBy(Member member) {
        return this.member.equals(member);
    }

    public void updateLocation(String city, String country) {
        this.city = this.city.equals("UNDEFINED") ? city : this.city;
        this.country = this.country.equals("UNDEFINED") ? country : this.country;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public boolean notOwnedBy(Long memberId) {
        return !isOwnedBy(memberId);
    }
}

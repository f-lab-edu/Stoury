package com.stoury.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "FEED_RESPONSE")
@NoArgsConstructor
@Getter
public class FeedResponseEntity {
    @Id
    @Column(name = "FEED_ID")
    private Long feedId;
    @Column(name = "WRITER_ID")
    private Long writerId;
    @Column(name = "WRITER_USERNAME")
    private String writerUsername;
    @Column(name = "GRAPHIC_CONTENT_PATHS", columnDefinition = "TEXT")
    private String graphicContentPaths;
    @Column(name = "TAG_NAMES", columnDefinition = "TEXT")
    private String tagNames;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    @Column(name = "TEXT_CONTENT")
    private String textContent;
    @Column(name = "LATITUDE")
    private Double latitude;
    @Column(name = "LONGITUDE")
    private Double longitude;
    @Column(name = "CITY")
    private String city;
    @Column(name = "COUNTRY")
    private String country;

    @Builder
    public FeedResponseEntity(Long feedId,
                              Long writerId,
                              String writerUsername,
                              String graphicContentPaths,
                              String tagNames,
                              LocalDateTime createdAt,
                              String textContent,
                              Double latitude,
                              Double longitude,
                              String city,
                              String country) {
        this.feedId = feedId;
        this.writerId = writerId;
        this.writerUsername = writerUsername;
        this.graphicContentPaths = graphicContentPaths;
        this.tagNames = tagNames;
        this.createdAt = createdAt;
        this.textContent = textContent;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.country = country;
    }

    public void update(String graphicContentPaths,
                       String tagNames,
                       LocalDateTime createdAt,
                       String textContent,
                       Double latitude,
                       Double longitude,
                       String city,
                       String country){
        this.graphicContentPaths = graphicContentPaths;
        this.tagNames = tagNames;
        this.createdAt = createdAt;
        this.textContent = textContent;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.country = country;
    }
}

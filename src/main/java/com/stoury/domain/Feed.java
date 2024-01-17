package com.stoury.domain;

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
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FEED")
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Column(name = "CREATED_AT")
    @CreatedDate
    private LocalDateTime createdAt;

    @BatchSize(size = 10)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "feed")
    private List<GraphicContent> graphicContents = new ArrayList<>();

    @Column(name = "TEXT_CONTENT", nullable = false, columnDefinition = "text")
    private String textContent;

    @Column(name = "LATITUDE")
    private Double latitude;

    @Column(name = "LONGITUDE")
    private Double longitude;

    @Builder
    public Feed(Member member, String textContent, Double latitude, Double longitude) {
        this.member = member;
        this.textContent = textContent;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void addGraphicContent(GraphicContent graphicContent) {
        graphicContent.beAttachedTo(this);
        graphicContents.add(graphicContent);
    }

    public void addGraphicContents(List<GraphicContent> graphicContentsPaths) {
        graphicContentsPaths.forEach(this::addGraphicContent);
    }
}
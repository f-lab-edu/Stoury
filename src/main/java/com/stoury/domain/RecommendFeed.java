package com.stoury.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "RECOMMEND_FEED")
@NoArgsConstructor
@Getter
public class RecommendFeed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "MEMBER_ID")
    private Long memberId;
    @Column(name = "FEED_ID")
    private Long feedId;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public RecommendFeed(Long memberId, Long feedId, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.feedId = feedId;
        this.createdAt = createdAt;
    }
}

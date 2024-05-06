package com.stoury.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "CLICK_LOG")
public class ClickLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "MEMBER_ID")
    private Long memberId;
    @Column(name = "FEED_ID")
    private Long feedId;
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Builder
    public ClickLog(Long memberId, Long feedId, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.feedId = feedId;
        this.createdAt = createdAt;
    }
}

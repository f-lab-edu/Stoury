package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "DIARY")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @JoinColumn(name = "DIARY_ID", referencedColumnName = "ID")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = false)
    private List<Feed> feeds = new ArrayList<>();

    @Column(name = "TITLE", length = 50)
    private String title;

    @JoinColumn(name = "THUMBNAIL_ID")
    @OneToOne(optional = true)
    private GraphicContent thumbnail;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    public Diary(Member member, List<Feed> feeds, String title, GraphicContent thumbnail) {
        this.member = member;
        this.feeds = feeds;
        this.title = title;
        this.thumbnail = thumbnail;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public boolean notOwnedBy(Long memberId) {
        return !isOwnedBy(memberId);
    }
}

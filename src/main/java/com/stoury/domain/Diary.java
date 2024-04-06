package com.stoury.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@DynamicInsert
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "DIARY")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "DIARY_ID", referencedColumnName = "ID")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = false)
    private List<Feed> feeds = new ArrayList<>();

    @Column(name = "TITLE", length = 50)
    private String title;

    @JoinColumn(name = "THUMBNAIL_ID")
    @OneToOne(optional = true)
    private GraphicContent thumbnail;

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

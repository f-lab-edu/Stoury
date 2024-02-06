package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "DIARY")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @JoinColumn(name = "DIARY_ID")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = false)
    private List<Feed> feeds = new ArrayList<>();

    @Column(name = "TITLE", length = 50)
    private String title;

    public Diary(Member member, List<Feed> feeds, String title) {
        this.member = member;
        this.feeds = feeds;
        this.title = title;
    }
}

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
@Table(name = "TAG")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TAG_NAME", nullable = false, length = 15, unique = true)
    private String tagName;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private List<Feed> feeds = new ArrayList<>();

    public Tag(String tagName) {
        this.tagName = tagName;
    }
}

package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "GRAPHIC_CONTENT")
public class GraphicContent {
    public static final String PATH_PREFIX = "/path/";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Feed feed;

    @Column(name = "PATH", unique = true, nullable = false)
    private String path;

    @Column(name = "SEQUENCE", columnDefinition = "TINYINT", nullable = false)
    private int sequence;

    public void beAttachedTo(Feed feed) {
        this.feed = feed;
    }

    public GraphicContent (int sequence) {
        this.path = PATH_PREFIX + LocalDateTime.now().getNano();
        this.sequence = sequence;
    }
}

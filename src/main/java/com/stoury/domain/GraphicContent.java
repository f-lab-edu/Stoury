package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "GRAPHIC_CONTENT")
public class GraphicContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Feed feed;

    @Column(name = "PATH", unique = true, nullable = false)
    private String path;

    public GraphicContent(String path) {
        this.path = path;
    }

    public void beAttachedTo(Feed feed) {
        this.feed = feed;
    }
}

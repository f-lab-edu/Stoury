package com.stoury.domain;

import com.stoury.utils.FileUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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

    @Column(name = "SEQUENCE", columnDefinition = "TINYINT", nullable = false)
    private int sequence;

    public void beAttachedTo(Feed feed) {
        this.feed = feed;
    }

    public GraphicContent(String path, int sequence) {
        this.path = path;
        this.sequence = sequence;
    }
}

package com.stoury.domain;

import com.stoury.utils.FileUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "GRAPHIC_CONTENT")
public class GraphicContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PATH", unique = true, nullable = false)
    private String path;

    @Setter(value = AccessLevel.PACKAGE)
    @Column(name = "SEQUENCE", columnDefinition = "TINYINT", nullable = false)
    private int sequence;

    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PACKAGE)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Feed feed;

    public GraphicContent(String path, int sequence) {
        this.path = path;
        this.sequence = sequence;
    }

    public boolean isImage() {
        return FileUtils.isImage(path);
    }
}

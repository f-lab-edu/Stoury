package com.stoury.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@DynamicInsert
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "COMMENT")
public class Comment {
    public static final String DELETED_CONTENT_TEXT = "This comment was deleted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Feed feed;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parentComment")
    private List<ChildComment> childComments = new ArrayList<>();

    @Setter(value = AccessLevel.PACKAGE)
    @Column(name = "HAS_CHILD_COMMENTS", nullable = false)
    private boolean hasChildComments;

    @Column(name = "TEXT_CONTENT", length = 200, nullable = false)
    private String textContent;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted;

    public Comment(Member member, Feed feed, String textContent) {
        this.member = member;
        this.feed = feed;
        this.textContent = textContent;
    }

    public Comment(Member member, String textContent) {
        this.member = member;
        this.textContent = textContent;
    }

    public boolean hasChildComments() {
        return hasChildComments;
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public boolean notOwnedBy(Long memberId) {
        return !isOwnedBy(memberId);
    }
}

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
@Table(name = "COMMENT")
public class Comment {
    public static final String DELETED_CONTENT_TEXT = "This comment was deleted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = false)
    private Feed feed;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parentComment")
    private List<Comment> childComments = new ArrayList<>();

    @Column(name = "HAS_CHILD_COMMENTS", nullable = false)
    private boolean hasChildComments;

    @ManyToOne(optional = true)
    @JoinColumn(name = "PARENT_COMMENT_ID")
    private Comment parentComment;

    @Column(name = "TEXT_CONTENT", length = 200, nullable = false)
    private String textContent;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted;

    public Comment(Member member, Feed feed, String textContent) {
        this.member = member;
        this.feed = feed;
        this.textContent = textContent;
    }

    public Comment(Member member, Comment parentComment, String textContent) {
        this(member, parentComment.getFeed(), textContent);

        this.parentComment = parentComment;
        parentComment.hasChildComments = true;
    }

    public boolean hasParent() {
        return parentComment != null;
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

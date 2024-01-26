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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = false)
    private Feed feed;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parentComment")
    List<Comment> nestedComments = new ArrayList<>();

    @ManyToOne(optional = true)
    @JoinColumn(name = "PARENT_COMMENT_ID")
    Comment parentComment;

    @Column(name = "TEXT_CONTENT", length = 200, nullable = false)
    private String textContent;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "DELETE", nullable = false)
    private boolean deleted;

    public Comment(Member member, Feed feed, String textContent) {
        this.member = member;
        this.feed = feed;
        this.textContent = textContent;
    }

    public Comment(Member member, Comment parentComment, String textContent) {
        this(member, parentComment.getFeed(), textContent);

        this.parentComment = parentComment;
    }

    public boolean hasParent() {
        return parentComment != null;
    }

    public boolean hasNestedComments() {
        return !nestedComments.isEmpty();
    }

    public void delete() {
        this.deleted = true;
    }
}

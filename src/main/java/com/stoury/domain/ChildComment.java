package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@DynamicInsert
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CHILD_COMMENT")
public class ChildComment {
    public static final String DELETED_CONTENT_TEXT = "This comment was deleted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_COMMENT_ID")
    private Comment parentComment;

    @Column(name = "TEXT_CONTENT", length = 200, nullable = false)
    private String textContent;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted;

    public ChildComment(Member member, Comment parentComment, String textContent) {
        this.member = member;
        this.parentComment = parentComment;
        this.textContent = textContent;
        this.parentComment.setHasChildComments(true);
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

package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "LIKE", uniqueConstraints = @UniqueConstraint(name = "LIKE_UNIQUE_CONSTRAINTS",
        columnNames = {"MEMBER_ID", "FEED_ID"}))
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;

    @ManyToOne(optional = false)
    private Feed feed;

    public Like(Member member, Feed feed) {
        this.member = member;
        this.feed = feed;
    }
}

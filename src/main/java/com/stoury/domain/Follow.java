package com.stoury.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "FOLLOW")
@NoArgsConstructor
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member follower;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member followee;

    @Builder
    public Follow(Member follower, Member followee) {
        this.follower = follower;
        this.followee = followee;
    }
}

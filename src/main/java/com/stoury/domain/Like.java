package com.stoury.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {
    private Member member;

    private Feed feed;

    public Like(Member member, Feed feed) {
        this.member = member;
        this.feed = feed;
    }
}

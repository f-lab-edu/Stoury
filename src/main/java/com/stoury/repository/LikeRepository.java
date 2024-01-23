package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Like;
import com.stoury.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByMemberAndFeed(Member member, Feed feed);

    void deleteByMemberAndFeed(Member member, Feed feed);

    Optional<Integer> countByFeed(Feed feed);
}

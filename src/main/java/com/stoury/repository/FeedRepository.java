package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    Slice<Feed> findAllByMemberAndCreatedAtIsBefore(Member feedWriter, LocalDateTime orderThan, Pageable page);

    @Query("""
            select f
            from Feed f join fetch f.tags t
            where t.tagName=:tag and f.createdAt<:orderThan
            """)
    Slice<Feed> findByTagAndCreateAtLessThan(String tag, LocalDateTime orderThan, Pageable page);
}

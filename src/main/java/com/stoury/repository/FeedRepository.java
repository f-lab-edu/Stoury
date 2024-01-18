package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    Slice<Feed> findByMember(Member feedWriter, Pageable page);
}

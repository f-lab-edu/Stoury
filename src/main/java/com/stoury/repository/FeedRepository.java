package com.stoury.repository;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    List<Feed> findAllByMemberAndCreatedAtIsBefore(Member feedWriter, LocalDateTime orderThan, Pageable page);

    @Query("""
            SELECT f
            FROM Feed f JOIN FETCH f.tags t
            WHERE t.tagName=:tag AND f.createdAt<:orderThan
            """)
    List<Feed> findByTagAndCreateAtLessThan(String tag, LocalDateTime orderThan, Pageable page);

    @Query(""" 
            SELECT f.city
            FROM Feed f 
            WHERE f.country = 'South Korea' 
            GROUP BY f.city 
            HAVING f.city <> 'UNDEFINED'
            ORDER BY COUNT(f) DESC
            """)
    List<String> findTop10CitiesInKorea(Pageable pageable);

    @Query("""
            SELECT f.country 
            FROM Feed f 
            WHERE f.country <> 'South Korea' 
            GROUP BY f.country 
            HAVING f.country <> 'UNDEFINED'
            ORDER BY COUNT(f) DESC
            """)
    List<String> findTop10CountriesNotKorea(Pageable pageable);
}

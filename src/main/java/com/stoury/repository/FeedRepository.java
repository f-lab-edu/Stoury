package com.stoury.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QFeed.feed;
import static com.stoury.domain.QTag.tag;

@Repository
@RequiredArgsConstructor
public class FeedRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    public Feed save(Feed saveFeed) {
        entityManager.persist(saveFeed);
        return saveFeed;
    }

    public void delete(Feed deleteFeed) {
        jpaQueryFactory
                .delete(feed)
                .where(feed.eq(deleteFeed))
                .execute();
    }

    public Optional<Feed> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(feed)
                .where(feed.id.eq(id))
                .fetchFirst());
    }

    public List<Feed> findAllByMemberAndIdLessThan(Member feedWriter, Long offsetId, Pageable page) {
        return jpaQueryFactory
                .selectFrom(feed)
                .where(feed.member.eq(feedWriter)
                        .and(feed.id.lt(offsetId)))
                .orderBy(feed.createdAt.desc())
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    public List<Feed> findByTagNameAndIdLessThan(String tagName, Long offsetId, Pageable page) {
        return jpaQueryFactory
                .selectFrom(feed)
                .leftJoin(feed.tags, tag).on(tag.tagName.eq(tagName))
                .where(feed.id.lt(offsetId))
                .orderBy(feed.createdAt.desc())
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    public List<String> findTop10CitiesInKorea(Pageable pageable) {
        return findTop10(feed.country.eq("South Korea"), pageable);
    }

    public List<String> findTop10CountriesNotKorea(Pageable pageable){
        return findTop10(feed.country.ne("South Korea"), pageable);
    }

    private List<String> findTop10(BooleanExpression expression, Pageable pageable) {
        return jpaQueryFactory
                .select(feed.country)
                .select(feed.city)
                .from(feed)
                .where(expression)
                .groupBy(feed.city)
                .having(feed.city.ne("UNDEFINED"))
                .orderBy(feed.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}

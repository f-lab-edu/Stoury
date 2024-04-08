package com.stoury.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.feed.FeedResponse;
import com.stoury.projection.FeedResponseEntity;
import com.stoury.projection.QFeedResponseEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QFeed.feed;
import static com.stoury.domain.QTag.tag;
import static com.stoury.projection.QFeedResponseEntity.*;

@Repository
@RequiredArgsConstructor
public class FeedRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public Feed save(Feed saveFeed) {
        entityManager.persist(saveFeed);
        entityManager.refresh(saveFeed);
        return saveFeed;
    }

    @Transactional
    public FeedResponseEntity saveFeedResponse(FeedResponseEntity saveFeedResponseEntity) {
        entityManager.persist(saveFeedResponseEntity);
        return saveFeedResponseEntity;
    }

    @Transactional
    public Optional<FeedResponseEntity> findFeedResponseById(Long feedId) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(feedResponseEntity)
                .where(feedResponseEntity.feedId.eq(feedId))
                .fetchFirst());
    }

    @Transactional
    public void delete(Feed deleteFeed) {
        jpaQueryFactory
                .delete(feed)
                .where(feed.eq(deleteFeed))
                .execute();
    }

    @Transactional(readOnly = true)
    public Optional<Feed> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(feed)
                .where(feed.id.eq(id))
                .fetchFirst());
    }

    @Transactional(readOnly = true)
    public List<FeedResponseEntity> findAllFeedsByMemberAndIdLessThan(Member feedWriter, Long offsetId, Pageable page){
        return jpaQueryFactory
                .selectFrom(feedResponseEntity)
                .where(feedResponseEntity.feedId.lt(offsetId)
                        .and(feedResponseEntity.writerId.eq(feedWriter.getId())))
                .orderBy(feedResponseEntity.feedId.desc())
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    private List<Feed> findAllIn(List<Long> ids) {
        return jpaQueryFactory
                .selectFrom(feed)
                .where(feed.id.in(ids))
                .orderBy(feed.id.desc())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Feed> findByTagNameAndIdLessThan(String tagName, Long offsetId, Pageable page) {
        List<Long> ids = findAllFeedIdByTagAndIdLessThan(tagName, offsetId, page);

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return findAllIn(ids);
    }

    private List<Long> findAllFeedIdByTagAndIdLessThan(String tagName, Long offsetId, Pageable page) {
        return jpaQueryFactory
                .select(feed.id)
                .from(feed)
                .innerJoin(feed.tags, tag).on(tag.tagName.eq(tagName))
                .where(feed.id.lt(offsetId))
                .orderBy(feed.id.desc())
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<String> findTop10CitiesInKorea(Pageable pageable) {
        return findTop10(feed.city, feed.country.eq("South Korea"), pageable);
    }

    @Transactional(readOnly = true)
    public List<String> findTop10CountriesNotKorea(Pageable pageable) {
        return findTop10(feed.country, feed.country.ne("South Korea"), pageable);
    }

    @Transactional(readOnly = true)
    public List<String> findTop10(StringPath column, BooleanExpression expression, Pageable pageable) {
        return jpaQueryFactory
                .select(column)
                .from(feed)
                .where(expression)
                .groupBy(column)
                .having(column.ne("UNDEFINED"))
                .orderBy(feed.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.selectFrom(feed).fetch().forEach(entityManager::remove);
    }

    @Transactional
    public void deleteAllFeedResponse() {
        jpaQueryFactory.selectFrom(feedResponseEntity).fetch().forEach(entityManager::remove);
    }

    @Transactional
    public Feed saveAndFlush(Feed saveFeed) {
        entityManager.persist(saveFeed);
        return saveFeed;
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return jpaQueryFactory
                .selectFrom(feed)
                .where(feed.id.eq(id))
                .fetchFirst() != null;
    }

    @Transactional
    public List<Feed> saveAll(Collection<Feed> feeds) {
        return feeds.stream().map(this::save).toList();
    }
}
